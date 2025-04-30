import cv2
import pytesseract
pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'

def extract_numbers_from_camera():
    video_capture = cv2.VideoCapture(0)

    while True:
        ret, frame = video_capture.read()
        if not ret:
            print("Error: Unable to capture frame")
            break
        gray_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        extracted_text = pytesseract.image_to_string(gray_frame)
        numbers = [int(num) for num in extracted_text.split() if num.isdigit() and len(num) == 8]
        cv2.imshow("Camera Feed", frame)
        if numbers:
            print(numbers[0])
            break
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break
    video_capture.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    extract_numbers_from_camera()