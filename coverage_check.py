import re

with open('target/site/jacoco/jacoco.xml', encoding='utf-8') as f:
    content = f.read()

classes = re.findall(r'<class name="([^"]+)".*?</class>', content, re.S)
total_missed = 0
total_covered = 0
per_class = []
for c in classes:
    name = re.match(r'<class name="([^"]+)"', c)
    cname = name.group(1) if name else "?"
    if cname.startswith("co/edu/escuelaing/techcup/logistics/dto/") \
       or cname.startswith("co/edu/escuelaing/techcup/logistics/entity/") \
       or cname.startswith("co/edu/escuelaing/techcup/logistics/config/") \
       or cname == "co/edu/escuelaing/techcup/logistics/ServiceLogisticsApplication":
        continue
    counters = re.findall(r'<counter type="LINE" missed="(\d+)" covered="(\d+)"/>', c)
    if not counters:
        continue
    missed, covered = map(int, counters[-1])
    total_missed += missed
    total_covered += covered
    total = missed+covered
    ratio = covered/total if total else 1
    per_class.append((cname, missed, covered, ratio))

per_class.sort(key=lambda x: x[3])
for cname, missed, covered, ratio in per_class:
    print(f"{ratio:.2%}  {cname}  (missed={missed}, covered={covered})")

overall = total_covered/(total_covered+total_missed)
print("\nOVERALL:", total_covered, "/", total_covered+total_missed, f"= {overall:.2%}")
