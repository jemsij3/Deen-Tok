with open('app/src/main/res/values/themes.xml', 'r') as f:
    content = f.read()

content = content.replace('parent="Theme.AppCompat.Light.NoActionBar"', 'parent="android:Theme.Material.Light.NoActionBar"')

with open('app/src/main/res/values/themes.xml', 'w') as f:
    f.write(content)
print("Updated themes.xml")
