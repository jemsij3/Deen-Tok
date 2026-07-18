import os
import requests
import json
import base64
import sys

def create_gist(filename, gist_name):
    print("Reading file...")
    with open(filename, 'rb') as f:
        content = f.read()

    # Files > 10MB can't be uploaded directly easily without a proper server
    # Wait, github releases might be better but we need a repo access.
    pass
