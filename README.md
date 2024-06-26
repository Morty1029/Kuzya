# Kuzya
Средство, позволяющее управлять умным домом при помощи не только голоса, но и жестов.

Реализован сервис, который по кнопке START SERVICE запускает камеру (ее видно на рисунке ниже), которая начинает в фоновом режиме делать снимки каждую секунду. Эти фотографии отдаются модулю с НС, определяющему показываемый жест. В файл JSON сохраняются для каждого "умного" устройства соответствия жестов и команд. Окно свободно можно перемещать по экрану. При сворачивании приложения окно остается видимым для просмотра попадаемых в ракурс объектов.

![image](https://github.com/Kattarinea/Kuzya/assets/65298723/513c4b12-2d39-4fb4-a3f8-f28bd797f89f)
![image](https://github.com/Kattarinea/Kuzya/assets/65298723/424b2f0c-c14a-4404-8eb8-1f69d5695187)



По кнопке GESTURE MANAGER можно настроить соответсвия жестов и команд для каждого устройства. На рисунке ниже представлен перечень доступных "умных" девайсов (второй объект является вымышленным, добавленным для наглядности, так как у нас всего одна лампа :) ). С помощью выпадающего списка можно настроить такие команды как: включить/выключить свет, сделать цвет света красным/оранжевым/белым/...

![image](https://github.com/Kattarinea/Kuzya/assets/65298723/795c9ce0-c8d6-4da5-a0e4-5eaae0f08903)
![image](https://github.com/Kattarinea/Kuzya/assets/65298723/009bf783-6f2a-4460-bc1a-6de4cf13dcc1)
