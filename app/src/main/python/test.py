import cv2 as cv
import numpy as np

def draw(bytesPar):
    bytes = str(bytesPar)
    nparr = np.fromstring(bytes, np.uint8)
    # nparr = np.frombuffer(bytearray(bytes), np.uint8)
    src = cv.imdecode(nparr, cv.IMREAD_COLOR)
    gr = cv.cvtColor(src, cv.COLOR_BGR2GRAY)
    canny = cv.Canny(gr, 10, 250)
    kernel = cv.getStructuringElement(cv.MORPH_RECT, (7, 7))
    closed = cv.morphologyEx(canny, cv.MORPH_CLOSE, kernel)
    contours = cv.findContours(closed.copy(), cv.RETR_EXTERNAL, cv.CHAIN_APPROX_SIMPLE)[0]
    for cont in contours:
        sm = cv.arcLength(cont, True)
        apd = cv.approxPolyDP(cont, 0.02 * sm, True)
        if len(apd) == 4:
            cv.drawContours(src, [apd], -1, (0, 255, 0), 4)

        im_resize = cv.resize(src, (500, 500))
        is_success, im_buf_arr = cv.imencode(".jpg", im_resize)
        byte_im = im_buf_arr.tobytes()
        return byte_im
def test(testEl):
    bytesString = str(testEl)
    nparrTest = np.fromstring(bytesString, np.uint8)
    return np.array_str(nparrTest)
