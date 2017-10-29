
import sys
import bluetooth

import time
import win32api, win32con

print '[+] Client Started'
uuid = '8ce255c0-200a-11e0-ac64-0800200c9a66'
appName = "BLUETOOTHMOUSE"

android_x = 0.0
android_y = 0.0
mouse_x = 0
mouse_y = 0
gain = 1000.0
start_time = 0

print '[+] Looking for Bluetooth Service with name: %s' %appName
service_matches = bluetooth.find_service( uuid = uuid ) #, name = appName )

if len(service_matches) == 0:
    print "couldn't find the bluetooth service"
    sys.exit(0)


print '[+] Service Found'
first_match = service_matches[0]
port = first_match["port"]
name = first_match["name"]
host = first_match["host"]

print "connecting to \"%s\" on %s" % (name, host)

sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
sock.connect((host, port))

print "connected to %s" %name

while True:
	try:
		data = sock.recv(1024)
		if (len(data)== 0):
			break
		print "Data Received: %s\n" %data   # for debugging
		
		dataPoints = data.split("|")
		
		for dataPoint in dataPoints:
        if (len(dataPoint) > 0):
            points = dataPoint.split(",")
            if (points[0] == "pageup"):
                win32api.keybd_event(win32con.VK_PRIOR, 0, 0, 0)
                win32api.keybd_event(win32con.VK_PRIOR, 0, win32con.KEYEVENTF_KEYUP, 0)
            elif (points[0] == "pagedown"):
                win32api.keybd_event(win32con.VK_NEXT, 0, 0, 0)
                win32api.keybd_event(win32con.VK_NEXT, 0, win32con.KEYEVENTF_KEYUP, 0)
            elif (points[0] == "actiondown"):
                start_time = time.time()
                android_x = float(points[1])
                android_y = float(points[2])
                [mouse_x, mouse_y] = win32api.GetCursorPos()
            elif (points[0] == "actionmove"):
                dx = float(points[1]) - android_x
                dy = float(points[2]) - android_y
                win32api.SetCursorPos((mouse_x + int(dx * gain),
                                       mouse_y + int(dy * gain)))
            elif (points[0] == "actionup"):
                if ((time.time() - start_time) < 0.2):
                    x = mouse_x + int((float(points[1]) - android_x) * gain)
                    y = mouse_y + int((float(points[2]) - android_y) * gain)
                    win32api.mouse_event(win32con.MOUSEEVENTF_LEFTDOWN,x,y,0,0)
                    win32api.mouse_event(win32con.MOUSEEVENTF_LEFTUP,x,y,0,0)
		
	except KeyboardInterrupt:
		print "KeyboardInterrupt Occured...Exiting."
		break;
	else:
		print "Error occured..Exiting"
		break
		
sock.close()