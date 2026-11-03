"""Deterministic AppFunctions lab consumer. Preview is the default; no automatic send."""
import argparse
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
import getpass
import hashlib
import json
import os
from pathlib import Path
import re
import shlex
import subprocess
import sys

COPILOT = 'com.cercafunctions.app'
CHAT = 'com.example.chatapp'
DEFAULT_ADB = str(Path(os.environ.get('ANDROID_HOME', str(Path.home() / 'AppData/Local/Android/Sdk'))) / 'platform-tools' / ('adb.exe' if os.name == 'nt' else 'adb'))


@dataclass(frozen=True)
class Contact:
    name: str
    endpoint: str
    display: str


@dataclass(frozen=True)
class Proposal:
    contact: Contact
    message: str
    proposed_at: str
    basis: tuple

    @property
    def code(self):
        raw = json.dumps([self.contact.__dict__, self.message, self.proposed_at, self.basis], sort_keys=True, separators=(',', ':'))
        return hashlib.sha256(raw.encode()).hexdigest()[:12]


def instant(value):
    parsed = datetime.fromisoformat(value.replace('Z', '+00:00'))
    if parsed.tzinfo is None:
        raise ValueError('Evidence must contain an explicit offset.')
    return parsed.astimezone(timezone.utc)


def validate_evidence(arrival, assessment):
    status = assessment.get('status')
    if status in ['ON_TIME', 'INDETERMINATE']:
        return None
    if status != 'LATE' or type(assessment.get('signedMarginMinutes')) is not int or assessment['signedMarginMinutes'] >= 0:
        raise ValueError('Invalid comparison; no proposal.')
    for key in ['arrival', 'source', 'scenario']:
        if not arrival.get(key) or arrival[key] != assessment.get(key):
            raise ValueError('Arrival evidence differs; query again before proposing.')
    margin = -assessment['signedMarginMinutes']
    start = instant(assessment['meetingStart'])
    proposed = start + timedelta(minutes=margin)
    if instant(assessment['arrival']) <= start or instant(assessment['arrival']) > proposed:
        raise ValueError('Inconsistent comparison timing; no proposal.')
    source = assessment['source']
    if source not in ['REFERENCE', 'FIXTURE']:
        raise ValueError('Unsupported evidence source.')
    return margin, proposed, source


def make_proposal(arrival, assessment, contacts):
    evidence = validate_evidence(arrival, assessment)
    if evidence is None:
        return None
    if len(contacts) != 1:
        raise ValueError('Recipient absent or ambiguous. Clarify the query; no send.')
    margin, proposed, source = evidence
    at = proposed.strftime('%Y-%m-%d %H:%M UTC')
    label = 'duración de referencia' if source == 'REFERENCE' else 'escenario ficticio de demostración'
    message = f'Hola {contacts[0].name}, con mi {label} llegaría {margin} min tarde. ¿Podemos retrasar la reunión al {at}?'
    basis = tuple(assessment.get(k) for k in ['status', 'signedMarginMinutes', 'arrival', 'meetingStart', 'source', 'scenario'])
    return Proposal(contacts[0], message, at, basis)


class OneShotSend:
    """One attempt in this process; UNKNOWN is terminal, including timeout after a possible write."""
    def __init__(self, proposal):
        self.proposal = proposal
        self.state = 'PENDING'

    def execute(self, confirmation, revalidate, send):
        if self.state != 'PENDING':
            return self.state
        if confirmation != 'ENVIAR ' + self.proposal.code:
            self.state = 'CANCELLED'
            return self.state
        try:
            if revalidate() != self.proposal:
                self.state = 'CANCELLED'
                return self.state
        except Exception:
            self.state = 'CANCELLED'
            return self.state
        self.state = 'ATTEMPTED'
        try:
            send(self.proposal)
            self.state = 'SENT'
        except Exception:
            self.state = 'UNKNOWN'
        return self.state


def decode_object(value):
    if not isinstance(value, dict):
        return value
    if 'epochSecond' in value and 'nanoAdjustment' in value:
        stamp = datetime.fromtimestamp(value['epochSecond'][0], timezone.utc)
        stamp += timedelta(microseconds=value['nanoAdjustment'][0] // 1000)
        return stamp.isoformat().replace('+00:00', 'Z')
    result = {}
    for key, items in value.items():
        if not isinstance(items, list):
            result[key] = decode_object(items)
        elif key == 'messages':
            result[key] = [decode_object(item) for item in items]
        else:
            values = [decode_object(item) for item in items]
            result[key] = values[0] if len(values) == 1 else values
    return result


class Sdk:
    def __init__(self, adb=DEFAULT_ADB, serial=None):
        self.adb = [adb] + (['-s', serial] if serial else [])
        self.ids = {}

    def discover(self, package):
        r = subprocess.run(self.adb + ['shell', 'cmd', 'app_function', 'list-app-functions', '--package', package], stdin=subprocess.DEVNULL, capture_output=True, text=True, encoding='utf-8', timeout=30)
        if r.returncode:
            raise RuntimeError('Discovery failed. Check compatible device and installation.')
        metadata = json.loads(r.stdout)
        def function_ids(v):
            if isinstance(v, list):
                for child in v: yield from function_ids(child)
            elif isinstance(v, dict):
                for key, child in v.items():
                    if key == 'functionId':
                        yield from child if isinstance(child, list) else [child]
                    else:
                        yield from function_ids(child)
        for name in ['queryJourney', 'evaluateAuthorizedCommitment', 'searchContacts', 'sendMessage', 'searchMessages']:
            # ApplicationId and Kotlin service package can differ; discovery is package-filtered.
            matches = {v for v in function_ids(metadata) if isinstance(v, str) and v.endswith('#' + name)}
            if len(matches) == 1:
                self.ids[package, name] = matches.pop()
        return metadata

    def call(self, package, name, parameters):
        identifier = self.ids.get((package, name))
        if not identifier:
            raise RuntimeError('Required function not discovered: ' + name)
        remote = 'read -r params; cmd app_function execute-app-function --package ' + shlex.quote(package) + ' --function ' + shlex.quote(identifier) + ' --parameters "$params" --timeout-duration 20'
        r = subprocess.run(self.adb + ['shell', remote], input=json.dumps(parameters) + '\n', capture_output=True, text=True, encoding='utf-8', timeout=30)
        if r.returncode or r.stdout.lstrip().startswith('Error executing app function:'):
            code = re.search(r'code (\d+)', r.stdout + r.stderr)
            raise RuntimeError('SDK invocation failed' + (' (code ' + code.group(1) + ')' if code else '') + '; no automatic retry.')
        raw = json.loads(r.stdout)
        return [decode_object(v) for v in raw.get('androidAppfunctionsReturnValue', [])]

    def count_message(self, proposal):
        rows = self.call(CHAT, 'searchMessages', dict(query=proposal.message, endpointValue=proposal.contact.endpoint))
        return sum(1 for row in rows for m in row.get('messages', []) if m.get('messageBody') == proposal.message)


def prepare(sdk, reference, parameters, contact_query='Ana'):
    journey = sdk.call(COPILOT, 'queryJourney', parameters)[0]
    assessment = sdk.call(COPILOT, 'evaluateAuthorizedCommitment', dict(parameters, reference=reference))[0]
    if validate_evidence(journey, assessment) is None:
        return None
    rows = sdk.call(CHAT, 'searchContacts', dict(query=contact_query, contactType='INDIVIDUAL'))
    contacts = [Contact(r['contactDisplayName'], r['endpointValue'], r['endpointDisplayName']) for r in rows]
    return make_proposal(journey, assessment, contacts)


def main():
    # Windows redirects may otherwise use the legacy code page; CLI output is UTF-8.
    for stream in [sys.stdout, sys.stderr]:
        if hasattr(stream, 'reconfigure'):
            stream.reconfigure(encoding='utf-8')
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--departure', required=True)
    parser.add_argument('--zone', required=True)
    parser.add_argument('--utc-offset')
    parser.add_argument('--scenario', choices=['REFERENCE', 'DEMO_NORMAL', 'DEMO_DELAYED', 'DEMO_UNKNOWN'])
    parser.add_argument('--contact-query', default='Ana')
    parser.add_argument('--adb', default=DEFAULT_ADB)
    parser.add_argument('--serial')
    parser.add_argument('--interactive', action='store_true', help='Require human confirmation on a terminal; otherwise preview only.')
    args = parser.parse_args()
    if args.interactive and not sys.stdin.isatty():
        parser.error('--interactive requires a terminal; piped input cannot authorize a send.')
    time = datetime.fromisoformat(args.departure)
    if time.tzinfo is not None:
        parser.error('Use local departure plus explicit --zone.')
    parameters = dict(departure=dict(year=time.year, month=time.month, dayOfMonth=time.day, hour=time.hour, minute=time.minute, second=time.second, nanoOfSecond=time.microsecond * 1000), zone=dict(zoneID=args.zone))
    if args.utc_offset: parameters['utcOffset'] = args.utc_offset
    if args.scenario: parameters['scenario'] = args.scenario
    reference = getpass.getpass('Referencia temporal UC-007: ') if sys.stdin.isatty() else sys.stdin.readline().rstrip('\r\n')
    sdk = Sdk(args.adb, args.serial)
    sdk.discover(COPILOT); sdk.discover(CHAT)
    proposal = prepare(sdk, reference, parameters, args.contact_query)
    if proposal is None:
        print('Sin propuesta: a tiempo o llegada indeterminada. Cero invocaciones de envío.')
        return
    print(f'Destinatario: {proposal.contact.name} | {proposal.contact.display} | {proposal.contact.endpoint}')
    print('Texto exacto:\n' + proposal.message)
    print('Hora propuesta: ' + proposal.proposed_at + '\nCódigo: ' + proposal.code)
    before = sdk.count_message(proposal)
    if not args.interactive:
        print('PREVIEW: cero invocaciones de envío. Usa --interactive en terminal para revisar y decidir.')
        return
    try:
        confirmation = input('Para enviar escribe ENVIAR ' + proposal.code + '; cualquier otra respuesta cancela: ')
    except EOFError:
        confirmation = ''
    state = OneShotSend(proposal).execute(confirmation, lambda: prepare(sdk, reference, parameters, args.contact_query), lambda p: sdk.call(CHAT, 'sendMessage', dict(endpointValue=p.contact.endpoint, messageBody=p.message)))
    print('Estado: ' + state)
    if state == 'SENT':
        after = sdk.count_message(proposal)
        print('Mensaje local único verificado: ' + str(after - before == 1))
        if after - before != 1:
            raise RuntimeError('Local effect not verified; inspect ChatApp. Do not retry automatically.')
    elif state == 'UNKNOWN':
        print('Resultado incierto: comprueba ChatApp antes de otra acción. No se reintenta.')


if __name__ == '__main__':
    try:
        main()
    except (RuntimeError, ValueError, KeyError, IndexError, subprocess.TimeoutExpired) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
