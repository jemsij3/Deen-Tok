with open('.gitignore', 'r') as f:
    content = f.read()

content = content.replace('\ndebug.keystore', '')
content = content.replace('debug.keystore\n', '')
content = content.replace('debug.keystore', '')

with open('.gitignore', 'w') as f:
    f.write(content)
print("Fixed .gitignore")
