# -*- coding: utf-8 -*-
import requests
import json


def post_image(URL,img_file):
    # post image and return the response
    img = open(img_file, 'rb').read() # image JPG file as bytes

    cr_num = {'cr_num':"서울12가1234"} #차로 인식 차량번호 as string

    response = requests.post(URL, data=img, params=cr_num)

    return response


# 프로그램 main ,  ex) python exam_post.py --ip=197.214.8.15 --port=5001
if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description='EX UNMANNED LPR')
    parser.add_argument('--ip', dest='LPR_SERVER_IP', type=str, help='Set LPR SERVER IP')
    parser.add_argument('--port', dest='LPR_SERVER_PORT', type=int, help='Set LPR SERVER PORT') # 5001 ~ 5008


    args = parser.parse_args()

    SERVER_IP = str(args.LPR_SERVER_IP)
    SERVER_PORT = args.LPR_SERVER_PORT
    addr = 'http://{}:{}'.format(SERVER_IP,SERVER_PORT)

    URL = addr + '/image'
    SHUT_DOWN_URL = addr + '/shutdown'

    # prepare headers for http request
    # content_type = 'image/jpeg'
    # headers = {'content-type': content_type}

    for i in range(1):
        response = post_image(URL,'./temp.jpg')
        # 응답형태 dictionary : {'lap': '0.090', 'size': '2048:1300', 'port': '5001', 'platenum': '충남83바2212'} : 소요시간, 이미지 사이즈, port 번호 , 서버인식 차량번호

        print(json.loads(response.text))
