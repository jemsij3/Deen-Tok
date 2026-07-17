import re

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

# Make sure we don't have duplicate Application tags or anything wrong
# The error says "Deen Tok closed because this app has a bug".
# This usually happens on newer Android versions when exported="true" isn't explicitly defined,
# but it IS defined on MainActivity.
# Another possible issue is missing permissions or something else.
