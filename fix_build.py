import re

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

# Make sure debug build is signed properly with the debug keystore
content = content.replace('// signingConfig = signingConfigs.getByName("debugConfig")', 'signingConfig = signingConfigs.getByName("debugConfig")')

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)

print("Fixed build.gradle.kts")
