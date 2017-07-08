#!/usr/bin/env python
import cv2, sys
import serial

port = "/dev/ttyACM1"
baud = 9600

arduino = serial.Serial(port,baud, timeout=1)

if arduino.isOpen():
	print("Yay something's working")

#Define constants
DEVICE_NUMBER = 2
FONT_FACES = [
	cv2.FONT_HERSHEY_SIMPLEX,
	cv2.FONT_HERSHEY_PLAIN,
	cv2.FONT_HERSHEY_DUPLEX,
	cv2.FONT_HERSHEY_COMPLEX,
	cv2.FONT_HERSHEY_TRIPLEX,
	cv2.FONT_HERSHEY_COMPLEX_SMALL,
	cv2.FONT_HERSHEY_SCRIPT_SIMPLEX,
	cv2.FONT_HERSHEY_SCRIPT_COMPLEX
]

# Get XML file input
if len(sys.argv) > 1:
	XML_PATH = sys.argv[1]
else:
	print "Error: XML path not defined"
	sys.exit(1)

# Initialize the cascade classifier
faceCascade = cv2.CascadeClassifier("/home/linaro/workshop/frontalface_default.xml")
eyeCascade = cv2.CascadeClassifier("/home/linaro/workshop/haarcascade_eye.xml")

eyeClosedCounter = 0

#Initialize webcam
vc = cv2.VideoCapture(DEVICE_NUMBER)

# Check if webcam works
if vc.isOpened():
    	# Try to get the first frame
	retval, frame = vc.read()
else:
	# Exit the program
	sys.exit(1)

i = 0
faces = []

# If the webcam read is successful, loop indefinitely
while retval:

	# Define the frame which the program will show
	frame_show = frame

	# Convert frame to grayscale to perform facial detection
	frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

	if i % 5 == 0:
		# Detect objects and return an array of faces
		faces = faceCascade.detectMultiScale(
			frame,
			scaleFactor=1.2,
			minNeighbors=2,
			minSize=(50, 50),
			flags=cv2.cv.CV_HAAR_SCALE_IMAGE
		)
	if(len(faces)<1):
		arduino.write('0\r\n')
	# Draw a rectangle around the faces 
	for (x, y, w, h) in faces:
		cv2.rectangle(frame_show, (x, y), (x+w, y+h), (0, 0, 255), 2)
		face_roi = frame[y:y+h,x:x+w]
		eyes = eyeCascade.detectMultiScale(face_roi)
		if(len(eyes)<2):
			eyeClosedCounter+=1;
		else:
			eyeClosedCounter=0;
		if(eyeClosedCounter >5):
			cv2.putText(frame_show,"Eyes Closed", (10,500), FONT_FACES[1], 4,(255,0,0),2)
			arduino.write('1\r\n')
		else:
			arduino.write('0\r\n')
		for (e_x,e_y,e_w,e_h) in eyes:
			cv2.rectangle(frame_show, (x+e_x, y+e_y), (e_x+x+e_w, e_y+y+e_h), (0, 255, 0), 2)


	# Show the frame to the user
	cv2.imshow("Test", frame_show)

	# Read in the next frame
	retval, frame = vc.read()
	
	# Exit program if the ESCAPE key is pressed
	if cv2.waitKey(1) == 27:
		break

	i += 1
