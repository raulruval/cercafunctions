"""Extract only stop IDs/names from the public Renfe GTFS; never extract ZIP paths."""
import argparse, csv, hashlib, io, json, zipfile
from datetime import datetime, timezone
from pathlib import Path
from urllib.request import Request, urlopen

URL = "https://ssl.renfe.com/ftransit/Fichero_CER_FOMENTO/fomento_transit.zip"

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("--archive", type=Path, help="Reuse a previously downloaded original archive")
    args=parser.parse_args()
    if args.archive:
        content=args.archive.read_bytes()
    else:
        with urlopen(Request(URL,headers={"User-Agent":"Cercafunctions-POC/0.3"}),timeout=30) as response:
            content=response.read(40_000_001)
    assert len(content)<=40_000_000
    with zipfile.ZipFile(io.BytesIO(content)) as archive:
        assert archive.getinfo("stops.txt").file_size<=2_000_000
        rows=list(csv.DictReader(io.StringIO(archive.read("stops.txt").decode("utf-8-sig"))))
    stations=sorted([(row["stop_id"].strip(),row["stop_name"].strip()) for row in rows])
    assert len({item[0] for item in stations})==len(stations)
    assert all(a and b and not any(c in a+b for c in "\t\r\n") for a,b in stations)
    root=Path(__file__).resolve().parents[1]
    stamp=datetime.now(timezone.utc).isoformat()
    digest=hashlib.sha256(content).hexdigest()
    target=root/"core-data/src/main/resources/renfe-stations.tsv"
    target.parent.mkdir(parents=True,exist_ok=True)
    target.write_text(f"# Origen de los datos: Renfe Operadora | CC BY 4.0\n# {URL}\n# extractedAt={stamp} archiveSha256={digest}\n"+"".join(f"{a}\t{b}\n" for a,b in stations),encoding="utf-8",newline="\n")
    print(f"{len(stations)} station IDs/names written to {target}; sha256={digest}")

if __name__=="__main__":main()
