import os

filepath = 'app/build.gradle.kts'
with open(filepath, 'r') as f:
    content = f.read()

# Make passwords fallback to a default if not in env
content = content.replace('System.getenv("STORE_PASSWORD")', 'System.getenv("STORE_PASSWORD") ?: "android123"')
content = content.replace('System.getenv("KEY_PASSWORD")', 'System.getenv("KEY_PASSWORD") ?: "android123"')

# Uncomment the signing config for release
content = content.replace('// signingConfig = signingConfigs.getByName("release")', 'signingConfig = signingConfigs.getByName("release")')

with open(filepath, 'w') as f:
    f.write(content)

print("Updated build.gradle.kts")
