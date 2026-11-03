#!/usr/bin/env python3
"""Read-only SDK smoke client for this POC. Consent must be granted in the app UI."""
import argparse,json,os,shlex,subprocess,sys,time,getpass
from datetime import datetime
from pathlib import Path
PACKAGE='com.cercafunctions.app'

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action',choices=['discover','query','state','assessment'])
    parser.add_argument('--adb',default=str(Path(os.environ.get('ANDROID_HOME',str(Path.home()/'AppData/Local/Android/Sdk')))/'platform-tools'/('adb.exe' if os.name=='nt' else 'adb')))
    parser.add_argument('--departure',help='Explicit local date/time, e.g. 2026-11-04T08:00:00')
    parser.add_argument('--zone',help='Explicit IANA or fixed-offset zone')
    parser.add_argument('--scenario',choices=['REFERENCE','DEMO_NORMAL','DEMO_DELAYED','DEMO_UNKNOWN'])
    parser.add_argument('--utc-offset')
    parser.add_argument('--origin-id')
    parser.add_argument('--destination-id')
    args=parser.parse_args()
    def shell(*parts):
        command=[args.adb,'shell','cmd','app_function',*parts]
        # adb forwards inherited stdin even for discovery; preserve a piped assessment reference.
        result=subprocess.run(command,stdin=subprocess.DEVNULL,capture_output=True,text=True,encoding='utf-8',timeout=45)
        if result.returncode:raise RuntimeError(result.stderr.strip() or result.stdout.strip() or 'AppFunctions command failed')
        if result.stdout.lstrip().startswith('Error executing app function:'):raise RuntimeError(result.stdout.strip())
        return result.stdout
    listing=shell('list-app-functions','--package',PACKAGE)
    metadata=json.loads(listing)
    def strings(value):
        if isinstance(value,str):yield value
        elif isinstance(value,list):
            for child in value:yield from strings(child)
        elif isinstance(value,dict):
            for key,child in value.items():yield key;yield from strings(child)
    suffix='#evaluateAuthorizedCommitment' if args.action=='assessment' else '#queryJourney'
    identifiers={s for s in strings(metadata) if s.startswith('com.cercafunctions.appfunctions.') and s.endswith(suffix)}
    for _ in range(20):
        if identifiers:break
        time.sleep(0.5)
        listing=shell('list-app-functions','--package',PACKAGE)
        metadata=json.loads(listing)
        identifiers={s for s in strings(metadata) if s.startswith('com.cercafunctions.appfunctions.') and s.endswith(suffix)}
    if len(identifiers)!=1:raise RuntimeError('Expected one discovered function matching the action; install the compatible APK and wait for indexing')
    function=identifiers.pop()
    if args.action=='discover':print(listing.strip());return
    if args.action=='state':print(shell('get-app-function-states','--package',PACKAGE,'--function',function).strip());return
    if not args.departure or not args.zone:parser.error('query requires --departure and --zone')
    departure=datetime.fromisoformat(args.departure)
    if departure.tzinfo is not None:parser.error('--departure must be local; use --zone and optional --utc-offset')
    parameters={'departure':{'year':departure.year,'month':departure.month,'dayOfMonth':departure.day,
        'hour':departure.hour,'minute':departure.minute,'second':departure.second,'nanoOfSecond':departure.microsecond*1000},
        'zone':{'zoneID':args.zone}}
    for key,value in [('scenario',args.scenario),('utcOffset',args.utc_offset),('originStationId',args.origin_id),('destinationStationId',args.destination_id)]:
        if value is not None:parameters[key]=value
    if args.action=='assessment':
        if args.origin_id is not None or args.destination_id is not None:parser.error('assessment does not accept station IDs')
        reference=getpass.getpass('Temporary reference: ') if sys.stdin.isatty() else sys.stdin.readline().rstrip('\r\n')
        parameters['reference']=reference
        # Pass sensitive JSON through stdin, so host-side adb arguments also contain no token.
        remote='read -r params; cmd app_function execute-app-function --package '+shlex.quote(PACKAGE)+' --function '+shlex.quote(function)+' --parameters "$params" --timeout-duration 20'
        result=subprocess.run([args.adb,'shell',remote],input=json.dumps(parameters,separators=(',',':'))+'\n',capture_output=True,text=True,encoding='utf-8',timeout=45)
        if result.returncode or result.stdout.lstrip().startswith('Error executing app function:'):
            import re
            code=re.search(r'code (\d+)',result.stdout+result.stderr)
            raise RuntimeError('Assessment invocation failed'+(' (code '+code.group(1)+')' if code else '')+'.')
        if reference and reference in result.stdout:raise RuntimeError('Unexpected sensitive SDK output suppressed.')
        print(result.stdout.strip());return
    # adb shell performs a second parsing stage in Android's POSIX shell.
    payload=shlex.quote(json.dumps(parameters,separators=(',',':')))
    print(shell('execute-app-function','--package',PACKAGE,'--function',function,'--parameters',payload,'--timeout-duration','20').strip())

if __name__=='__main__':
    try:main()
    except (RuntimeError,ValueError,subprocess.TimeoutExpired) as error:
        print(str(error),file=sys.stderr);sys.exit(1)
