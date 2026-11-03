"""Build/install the pinned official Android samples with one explicitly synthetic Ana contact."""
import argparse
import os
from pathlib import Path
import subprocess

PIN = '0bb367a2b7ef276218ac37f8eb45f6c9a81ca0cd'
ROOT = Path(__file__).resolve().parents[1]
CONTACT = '                Recipient(id = "uc008-ana", name = "Ana POC", email = "ana.poc@example.invalid"),'


def run(args, cwd=None, env=None):
    subprocess.run([str(a) for a in args], cwd=cwd, env=env, check=True, stdin=subprocess.DEVNULL)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--samples-dir', type=Path, default=ROOT / 'external/appfunctions')
    parser.add_argument('--sdk', type=Path, default=Path(os.environ.get('ANDROID_HOME', str(Path.home() / 'AppData/Local/Android/Sdk'))))
    parser.add_argument('--java-home', type=Path, default=Path('C:/Program Files/Android/Android Studio/jbr'))
    parser.add_argument('--prepare-only', action='store_true')
    args = parser.parse_args()
    samples = args.samples_dir.resolve()
    if not samples.exists():
        samples.parent.mkdir(parents=True, exist_ok=True)
        run(['git', '-c', 'core.longpaths=true', 'clone', 'https://github.com/android/appfunctions.git', samples])
        run(['git', '-c', 'core.longpaths=true', 'checkout', '--detach', PIN], samples)
    head = subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=samples, text=True).strip()
    if head != PIN:
        raise RuntimeError('Existing samples must be at the documented pin; use a new directory. No reset performed.')
    target = samples / 'ChatApp/shared/src/main/kotlin/com/example/chatapp/data/RecipientsRepository.kt'
    text = target.read_text(encoding='utf-8')
    anchor = '                Recipient(id = "7", name = "Bob Johnson", email = "bob2@example.com"),'
    if CONTACT not in text:
        if text.count(anchor) != 1:
            raise RuntimeError('Contact source differs; inspect instead of overwriting.')
        target.write_text(text.replace(anchor, anchor + '\n' + CONTACT), encoding='utf-8', newline='\n')
    env = dict(os.environ, JAVA_HOME=str(args.java_home))
    for folder, task, apk in [('ChatApp', ':app:assembleDebug', 'app/build/outputs/apk/debug/app-debug.apk'), ('agent', ':app:assembleStandardDebug', 'app/build/outputs/apk/standard/debug/app-standard-debug.apk')]:
        project = samples / folder
        (project / 'local.properties').write_text('sdk.dir=' + args.sdk.as_posix() + '\n', encoding='utf-8')
        if not args.prepare_only:
            run([project / ('gradlew.bat' if os.name == 'nt' else 'gradlew'), task], project, env)
            run([args.sdk / 'platform-tools' / ('adb.exe' if os.name == 'nt' else 'adb'), 'install', '-r', project / apk])
    print('Official pin:', PIN, '\nSamples:', samples, '\nAna POC is synthetic; message logic is unchanged.')
    print('Next: powershell -ExecutionPolicy Bypass -File scripts/start-testing-agent.ps1')


if __name__ == '__main__':
    main()
