import numpy as np
from PIL import Image
import torch
from torchvision import models
from torchvision import transforms as T


def recognize(path_to_image, path_to_model):
    class_names = [
        'dislike',
        'like',
        'ok',
        'rock',
        'no_gesture'
    ]

    transform = T.ToTensor()

    model = models.detection.ssdlite320_mobilenet_v3_large(num_classes=6, pretrained_backbone=True)
    model.load_state_dict(torch.load(path_to_model))

    images = []
    images.append(Image.open(path_to_image))
    # %%
    images_tensors = images.copy()
    images_tensors_input = list(transform(image).to(torch.device('cpu')) for image in images_tensors)
    # %%
    with torch.no_grad():
        model.eval()
        out = model(images_tensors_input)
    # %%
    # %%
    bboxes = []
    scores = []
    labels = []
    for pred in out:
        ids = pred['scores'] >= 0.2
        bboxes.append(pred['boxes'][ids][:2].cpu().numpy())
        scores.append(pred['scores'][ids][:2].cpu().numpy())
        labels.append(pred['labels'][ids][:2].cpu().numpy())
    # %%

        if labels[0][np.argmax(scores)] >= 5 or scores is None:
            return 'no_gesture'
        else:
            return class_names[labels[0][np.argmax(scores)]]