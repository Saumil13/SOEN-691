import re

pattern = re.compile("^(QUERY: [CRITERIA])+$")

file = open('C:\\Users\\100mil\\Downloads\\openmrs.log','r')
lines = file.readlines()
sqlLines = []

for each_line in lines:
    if "QUERY: [CRITERIA]" in each_line:
        #print(each_line)
        #print(each_line.split("QUERY: [CRITERIA]",1)[1])
        sqlLines.append(each_line.split("QUERY: [CRITERIA]",1)[1])


filename = "openmrs.sql"
try:
    file = open(filename, 'w')
except IOError:
    file = open(filename, 'w')

for item in sqlLines:
    print(item)
    file.write(item)

print("File written successfully.")
