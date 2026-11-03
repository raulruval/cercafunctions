import dataclasses
import sys
import unittest
from unittest.mock import patch
import io
import subprocess
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'scripts'))
import interop_flow
from interop_flow import Contact, OneShotSend, make_proposal


class ProposalTests(unittest.TestCase):
    def setUp(self):
        self.arrival = dict(arrival='2026-11-04T09:15:00Z', source='REFERENCE', scenario='REFERENCE')
        self.assessment = dict(self.arrival, status='LATE', signedMarginMinutes=-15, meetingStart='2026-11-04T09:00:00Z')
        self.ana = Contact('Ana POC', 'uc008-ana', 'ana.poc@example.invalid')

    def proposal(self):
        return make_proposal(self.arrival, self.assessment, [self.ana])

    def test_late_proposal_has_exact_contact_and_utc_time_without_calendar_text(self):
        p = self.proposal()
        self.assertEqual(self.ana, p.contact)
        self.assertIn('2026-11-04 09:15 UTC', p.message)
        self.assertIn('referencia', p.message)
        self.assertNotIn('meetingStart', p.message)

    def test_on_time_never_proposes(self):
        self.assessment.update(status='ON_TIME', signedMarginMinutes=0)
        self.assertIsNone(self.proposal())

    def test_unknown_never_proposes(self):
        self.assessment = dict(status='INDETERMINATE')
        self.assertIsNone(self.proposal())

    def test_missing_contact_requires_clarification(self):
        with self.assertRaises(ValueError):
            make_proposal(self.arrival, self.assessment, [])

    def test_ambiguous_contact_never_selects_first(self):
        with self.assertRaises(ValueError):
            make_proposal(self.arrival, self.assessment, [self.ana, Contact('Ana otra', 'other', 'other@example.invalid')])

    def test_mismatching_arrival_or_source_is_not_used(self):
        for key, value in [('arrival', '2026-11-04T09:16:00Z'), ('source', 'FIXTURE')]:
            with self.subTest(key=key), self.assertRaises(ValueError):
                make_proposal(self.arrival, dict(self.assessment, **{key: value}), [self.ana])

    def test_proposal_is_immutable(self):
        with self.assertRaises(dataclasses.FrozenInstanceError):
            self.proposal().message = 'changed'

    def test_editing_message_requires_new_confirmation_code(self):
        p = self.proposal()
        self.assertNotEqual(p.code, dataclasses.replace(p, message='Edited text').code)

    def test_empty_wrong_or_rejected_confirmation_never_sends(self):
        for confirmation in ['', 'NO', 'yes', 'ENVIAR wrong']:
            calls = []
            gate = OneShotSend(self.proposal())
            self.assertEqual('CANCELLED', gate.execute(confirmation, self.proposal, calls.append))
            self.assertEqual([], calls)

    def test_exact_confirmation_sends_once_even_if_confirmed_twice(self):
        calls = []
        p = self.proposal()
        gate = OneShotSend(p)
        for _ in range(2):
            self.assertEqual('SENT', gate.execute('ENVIAR ' + p.code, self.proposal, calls.append))
        self.assertEqual([p], calls)

    def test_changed_evidence_cancels_old_approval(self):
        p = self.proposal()
        calls = []
        changed = dataclasses.replace(p, message='Changed meeting')
        self.assertEqual('CANCELLED', OneShotSend(p).execute('ENVIAR ' + p.code, lambda: changed, calls.append))
        self.assertEqual([], calls)

    def test_lost_authorization_cancels_without_send(self):
        p = self.proposal()
        def unavailable():
            raise RuntimeError('Denied')
        self.assertEqual('CANCELLED', OneShotSend(p).execute('ENVIAR ' + p.code, unavailable, self.fail))

    def test_uncertain_send_is_consumed_before_io_and_never_retried(self):
        p = self.proposal()
        gate = OneShotSend(p)
        calls = []
        def uncertain(value):
            calls.append(value)
            self.assertEqual('ATTEMPTED', gate.state)
            raise TimeoutError()
        for _ in range(2):
            self.assertEqual('UNKNOWN', gate.execute('ENVIAR ' + p.code, self.proposal, uncertain))
        self.assertEqual(1, len(calls))

    def test_changed_endpoint_requires_new_confirmation(self):
        p = self.proposal()
        changed = dataclasses.replace(p, contact=dataclasses.replace(p.contact, endpoint='other'))
        self.assertNotEqual(p.code, changed.code)

    def test_cli_never_accepts_piped_confirmation(self):
        argv = ['interop_flow.py', '--departure', '2026-11-04T08:40', '--zone', 'UTC', '--interactive']
        with patch.object(sys, 'argv', argv), patch.object(sys, 'stdin', io.StringIO('ENVIAR anything\n')), patch('sys.stderr', io.StringIO()):
            with self.assertRaises(SystemExit) as failure:
                interop_flow.main()
            self.assertEqual(2, failure.exception.code)

    def test_discovery_never_consumes_reference_stdin(self):
        metadata = '[{"functionId":["com.cercafunctions.appfunctions.BaseJourneyAppFunctionService#queryJourney"],"id":"com.cercafunctions.app/com.cercafunctions.appfunctions.BaseJourneyAppFunctionService#queryJourney"}]'
        with patch('interop_flow.subprocess.run', return_value=subprocess.CompletedProcess([], 0, metadata, '')) as run:
            sdk = interop_flow.Sdk('adb')
            sdk.discover(interop_flow.COPILOT)
            self.assertIn((interop_flow.COPILOT, 'queryJourney'), sdk.ids)
            self.assertEqual(subprocess.DEVNULL, run.call_args.kwargs['stdin'])

    def test_on_time_stops_before_contact_lookup(self):
        calls = []
        class ReadOnlySdk:
            def call(inner, package, name, parameters):
                calls.append(name)
                return [self.arrival if name == 'queryJourney' else dict(status='ON_TIME')]
        self.assertIsNone(interop_flow.prepare(ReadOnlySdk(), 'synthetic', {}, 'Ana'))
        self.assertEqual(['queryJourney', 'evaluateAuthorizedCommitment'], calls)

    def test_inconsistent_evidence_stops_before_contact_lookup(self):
        calls = []
        class ReadOnlySdk:
            def call(inner, package, name, parameters):
                calls.append(name)
                return [self.arrival if name == 'queryJourney' else dict(self.assessment, source='FIXTURE')]
        with self.assertRaises(ValueError):
            interop_flow.prepare(ReadOnlySdk(), 'synthetic', {}, 'Ana')
        self.assertEqual(['queryJourney', 'evaluateAuthorizedCommitment'], calls)


if __name__ == '__main__':
    unittest.main()
