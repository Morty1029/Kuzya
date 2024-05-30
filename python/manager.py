from yeelight import Bulb, discover_bulbs
from HackBulb import *
def discover_all_smart_things():
    bulb_list = []
    bulbs = discover_bulbs()
    for bulb in bulbs:
        
        bulb_dict = {
            'ip': bulb['ip'],
            'port': bulb['port'],
            'name': bulb['capabilities']['name']
        }
        bulb_list.append(bulb_dict)
        #new_bulb.color_set(8)
    bulb_list.append({
            'ip': '111.222.333.444',
            'port': '0000',
            'name': 'PumPum'
        })
    return bulb_list
    
def invoked_commands(ip, command, arg):
    new_bulb = hackathon_bulb(ip)
    if hasattr(new_bulb, command):
        method_to_call = getattr(new_bulb, command)
        if arg!=0:
            method_to_call(arg)
        else:
            method_to_call()
        
    
    