
import sys
import bluetooth

print '[+] Client Started'
uuid = '8ce255c0-200a-11e0-ac64-0800200c9a66'
appName = "BLUETOOTHMOUSE"

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
		print "Data Received: %s\n" %data
	except KeyboardInterrupt:
		print "KeyboardInterrupt Occured...Exiting."
		break;
	else:
		print "Error occured..Exiting"
		break
		
sock.close()