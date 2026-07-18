import requests
import json
import sys

def upload_file(filename):
    with open(filename, 'rb') as f:
        response = requests.post('https://bashupload.com/', files={'file': f})
        print(response.text)

if __name__ == "__main__":
    if len(sys.argv) > 1:
        upload_file(sys.argv[1])
    else:
        print("Usage: python3 upload_apk.py <file>")
