"""Optional SDK fixture tool. Writes only a uniquely owned LOCAL calendar, never app data."""
import argparse
from datetime import datetime, timedelta, timezone
import json
import os
from pathlib import Path
import re
import shlex
import shutil
import subprocess
import uuid

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action',choices=['create','cleanup'])
    parser.add_argument('--adb',default=shutil.which('adb') or str(Path(os.environ.get('LOCALAPPDATA',''))/'Android/Sdk/platform-tools/adb.exe'))
    parser.add_argument('--date',help='Demo query date YYYY-MM-DD in UTC (default: tomorrow UTC)')
    parser.add_argument('--state',type=Path,default=Path(__file__).resolve().parents[1]/'.calendar-demo-state.json')
    args=parser.parse_args()

    def shell(*parts):
        response=subprocess.run([args.adb,'shell',shlex.join(parts)],capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=20)
        output=response.stdout+response.stderr
        if response.returncode or 'Exception' in output:raise RuntimeError(output)
        return output
    def save(state):args.state.write_text(json.dumps(state,indent=2)+'\n',encoding='utf-8')
    def query(uri,where=None):
        parts=['content','query','--uri',uri,'--projection','_id:account_name:account_type:name']
        if where:parts+=['--where',where]
        return shell(*parts)
    def insert(uri,values):
        parts=['content','insert','--uri',uri]
        for name,kind,value in values:parts+=['--bind',f'{name}:{kind}:{value}']
        shell(*parts)  # Some Android versions intentionally print no inserted URI.

    if args.action=='create':
        if args.state.exists() and not json.loads(args.state.read_text(encoding='utf-8')).get('cleanupVerified'):
            raise RuntimeError('An owned fixture is active. Run cleanup using the same state file first.')
        date=datetime.strptime(args.date,'%Y-%m-%d').date() if args.date else datetime.now(timezone.utc).date()+timedelta(days=1)
        account='cercafunctions-demo-'+uuid.uuid4().hex
        state={'account':account,'calendarId':None,'date':date.isoformat(),'zone':'UTC','cleanupVerified':False}
        save(state)
        uri=f'content://com.android.calendar/calendars?caller_is_syncadapter=true&account_name={account}&account_type=LOCAL'
        insert(uri,[('account_name','s',account),('account_type','s','LOCAL'),('name','s',account),
            ('calendar_displayName','s','POC demo UC-004'),('calendar_color','i',-16746133),('calendar_access_level','i',700),
            ('ownerAccount','s',account),('calendar_timezone','s','UTC'),('visible','i',1),('sync_events','i',1)])
        found=query('content://com.android.calendar/calendars',f"account_name='{account}' AND account_type='LOCAL'")
        ids=re.findall(r'_id=(\d+)',found)
        if len(ids)!=1:raise RuntimeError('Cannot identify the uniquely owned calendar; no event write performed.')
        state['calendarId']=int(ids[0]);save(state)
        for hour,recurring,title in [(9,True,'POC reunion recurrente'),(10,False,'POC otra reunion')]:
            start=datetime.combine(date-timedelta(days=1) if recurring else date,datetime.min.time(),timezone.utc)+timedelta(hours=hour)
            begin=int(start.timestamp()*1000)
            values=[('calendar_id','l',state['calendarId']),('title','s',title),('dtstart','l',begin),
                ('eventTimezone','s','UTC'),('allDay','i',0),('eventStatus','i',1)]
            values+= [('duration','s','PT30M'),('rrule','s','FREQ=DAILY;COUNT=3')] if recurring else [('dtend','l',begin+1800000)]
            insert('content://com.android.calendar/events',values)
        print(f"Demo ready: query date {date}, zone UTC; events 09:00 recurring / 10:00. Grant READ from the app UI.")
    else:
        state=json.loads(args.state.read_text(encoding='utf-8'))
        if state.get('cleanupVerified'):print('Fixture already removed.');return
        account=state['account'];cal=state['calendarId']
        if not re.fullmatch(r'cercafunctions-demo-[0-9a-f]{32}',account):raise RuntimeError('Invalid fixture owner; no deletion performed.')
        if cal is None:
            ids=re.findall(r'_id=(\d+)',query('content://com.android.calendar/calendars',f"account_name='{account}' AND account_type='LOCAL'"))
            if len(ids)!=1:raise RuntimeError('Cannot recover one owned calendar; no deletion performed.')
            cal=int(ids[0])
        if not isinstance(cal,int) or cal<=0:raise RuntimeError('Invalid fixture ID; no deletion performed.')
        uri=f'content://com.android.calendar/calendars/{cal}'
        owned=query(uri)
        if f'account_name={account}' not in owned or f'name={account}' not in owned or 'account_type=LOCAL' not in owned:
            raise RuntimeError('Fixture ownership changed; no deletion performed.')
        shell('content','delete','--uri',uri+f'?caller_is_syncadapter=true&account_name={account}&account_type=LOCAL')
        if '_id=' in query(uri):raise RuntimeError('Provider did not remove the owned fixture.')
        state['cleanupVerified']=True;save(state)
        print('Only the owned LOCAL fixture calendar and its events were removed.')

if __name__=='__main__':main()
