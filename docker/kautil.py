#!/usr/bin/env python3

import requests
import getopt
import sys
import urllib.parse

def build_url(base_url, params):
  """Builds an URL from base_url and params
  
  Arguments:
    base_url {string} -- base URL
    params {dict} -- parameters dict
  
  Returns:
    string -- URL string
  """
  return base_url + "?" + urllib.parse.urlencode(params)

def fetch_json(base_url, client_id, client_secret, params):
  """Makes GET request with given url, auth and params and returns response as JSON
  
  Arguments:
    base_url {string} -- base URL
    client_id {string} -- client id
    client_secret {string} -- client secret
    params {dict} -- parameters dict
  
  Raises:
    Exception -- Exception thrown when request fails 
  
  Returns:
    dict -- JSON response
  """
  url = build_url(base_url, params)
  response = requests.get(url, auth=(client_id, client_secret))
  if response.status_code == 200:
    return response.json()  
  else:
    raise Exception("Request (%s) failed with following message: [%d] %s" % (url, response.status_code, response.text))

def fetch_organization_setting(url, client_id, client_secret, organization_id, key):
  """Fetches an organization setting
  
  Arguments:
    url {string} -- Kunta API base URL
    client_id {string} -- client id
    client_secret {string} -- client secret
    organization_id {string} -- Kunta API organization id
    key {string} -- key of setting to be returned
  
  Returns:
    dict -- setting JSON or None if setting is not defined
  """
  json = fetch_json('%s/v1/organizations/%s/settings' % (url, organization_id), client_id, client_secret, {
    "key": key
  })

  if len(json) == 1:
    return json[0]
  
  return None

def get_organization_id(options):
  """Returns organization id
  
  Arguments:
    options {dict} -- program arguments
  
  Raises:
    Exception -- Thrown when organization id resolving fails
  
  Returns:
    string -- organization id
  """
  if not "business-name" in options and not "business-code" in options:
    raise Exception("business-name or business-code is required")

  url = options["url"]
  params = {}

  if "business-code" in options:
    params["businessCode"] = options["business-code"]

  if "business-name" in options:
    params["businessName"] = options["business-name"]

  organizations = fetch_json("%s/v1/organizations/" % (url), options["client-id"], options["client-secret"], params)
  if organizations == None:
    return ""

  organization_count = len(organizations)
  if organization_count == 1:
    return organizations[0]["id"]

  raise Exception("Query returned %d organizations" % (organization_count))

def get_organization_setting(options):
  """Returns organization setting
  
  Arguments:
    options {dict} -- program arguments
  
  Raises:
    Exception -- Thrown when organization setting resolving fails
  
  Returns:
    string -- organization setting value
  """
  if not "organization-id" in options or not "name" in options:
    raise Exception("organization-id and name are required")
    
  url = options["url"]
  organization_id = options["organization-id"]
  name = options["name"]
  client_id = options["client-id"]
  client_secret = options["client-secret"]
  current = fetch_organization_setting(url, client_id, client_secret, organization_id, name)

  if current == None:
    return ""
  else:
    return current["value"]

def set_organization_setting(options):
  """Changes an organization setting
  
  Arguments:
    options {dict} -- program arguments
  
  Raises:
    Exception -- Thrown when changing an organization setting fails
  
  Returns:
    string -- Exlanation what has been changed
  """
  if not "organization-id" in options or not "name" in options or not "value" in options:
    raise Exception("organization-id, name and value are required")

  url = options["url"]
  organization_id = options["organization-id"]
  name = options["name"]
  value = options["value"]
  client_id = options["client-id"]
  client_secret = options["client-secret"]
  current = fetch_organization_setting(url, client_id, client_secret, organization_id, name)

  if current == None:
    payload = {
      'value': value,
      'key': name
    }
    
    postResponse = requests.post('%s/v1/organizations/%s/settings' % (url, organization_id), json=payload, auth=(client_id, client_secret))
    if postResponse.status_code != requests.codes.ok:
      raise Exception("[%d] %s" % (postResponse.status_code, postResponse.text))

    return "Added new setting %s with value %s" % (name, value)

  elif current["value"] != value:
    payload = {
      'value': value,
      'key': name
    }

    putResponse = requests.put('%s/v1/organizations/%s/settings/%s' % (url, organization_id, current["id"]), json=payload, auth=(client_id, client_secret))
    if putResponse.status_code != requests.codes.ok:
      raise Exception("[%d] %s" % (putResponse.status_code, putResponse.text))

    return "Update setting %s from %s into %s" % (name, current["value"], value)
  else:
    return "Setting %s already set as %s" % (name, value)

def __main(argv):
  """Main method
  
  Arguments:
    argv {dict} -- program arguments
  
  Returns:
    number -- response code
  """
  available_options = {
    "command": { 
      "example": "get-organization-id|get-organization-setting|set-organization-setting",
      "required": True
    },
    "url": { 
      "example": "https://url.to-kunta-api:1234",
      "required": False,
      "default": "http://dev.kunta-api.fi:8080"
    },
    "client-id": { 
      "example": "client id",
      "required": True
    },
    "client-secret": { 
      "example": "client secret",
      "required": True
    },
    "business-name": { 
      "example": "Business name",
      "required": False
    },
    "business-code": { 
      "example": "Business code",
      "required": False
    },
    "organization-id": { 
      "example": "Organization id",
      "required": False
    },
    "name": { 
      "example": "name",
      "required": False
    },
    "value": { 
      "example": "value",
      "required": False
    }
  }

  get_options = []
  options = {}

  opt_error = "Usage: kautils.py"
  for available_option_name, available_option_settings in available_options.items():
    opt_error = "%s --%s=[%s]" % (opt_error, available_option_name, available_option_settings["example"])

  try:
    opts, args = getopt.getopt(argv,"hi:o:", list(map(lambda available_option_name: "%s=" % (available_option_name), available_options)))
  except getopt.GetoptError:
    print (opt_error)
    return 2

  for opt, arg in opts:
    options[opt[2:]] = arg

  for available_option_name, available_option_settings in available_options.items():
    if not available_option_name in options:
      if available_option_settings["required"]:
        print ("%s is required" % (available_option_name))
        return 2
      elif "default" in available_option_settings:
        options[available_option_name] = available_option_settings["default"]
    
  try:
    if (options["command"] == "get-organization-id"):
      print(get_organization_id(options))
    elif (options["command"] == "get-organization-setting"):
      print(get_organization_setting(options))
    elif (options["command"] == "set-organization-setting"):
      print(set_organization_setting(options))
  except Exception as error:
    print (error)
    return 2

  return 0

sys.exit(__main(sys.argv[1:]))