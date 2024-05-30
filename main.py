from flask import Flask, request, jsonify
import os
from func import recognize
app = Flask(__name__)

# Папка, в которую будут сохраняться фотографии
UPLOAD_FOLDER = 'uploads'
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

@app.route('/upload', methods=['POST'])
def upload_file():
    try:
        if 'image' not in request.files:
            return jsonify({'error': 'No image part'})

        file = request.files['image']

        if file.filename == '':
            return jsonify({'error': 'No selected file'})

        if file:
            filename = os.path.join(app.config['UPLOAD_FOLDER'], file.filename)
            file.save(filename)

            d = "D:/HTTPServer/uploads/" + file.filename
            res_str = recognize(d, 'D:/HTTPServer/14.pth')
            return jsonify({'message': res_str})

    except Exception as e:
        return jsonify({'error': str(e)})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5555)  # Запуск сервера на всех интерфейсах на порту 5555
