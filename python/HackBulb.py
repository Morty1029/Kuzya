from yeelight import Bulb, flows

brightness_score = 20
def make_a_color(color):
    match color:
        case 1:  # red
            return 255, 0, 0
        case 2:  # orange
            return 252, 102, 0
        case 3:  # yellow
            return 255, 255, 0
        case 4:  # green
            return 0, 255, 0
        case 5:  # cyan
            return 66, 170, 255
        case 6:  # blue
            return 0, 0, 255
        case 7:  # purple
            return 128, 0, 128
        case 8:  # white
            return 255, 255, 255

class hackathon_bulb(Bulb):
    def __init__(self, ip):
        super().__init__(ip)
        self.alarm_mode = 0

    def switch_on(self):
        self.set_color_temp(6500)
        self.turn_on()
 
    def switch_off(self):
        self.turn_off()

    def color_set(self, color):
        r, g, b = make_a_color(color)
        if r == 255 and g == 255 and b == 255:
            self.set_color_temp(6500)
        else:
            self.set_rgb(r, g, b)

    def set_brightness_more_less(self, mark):
        match mark:
            case 1:  # more
                self.set_brightness(int(self.get_properties()["current_brightness"]) + brightness_score)
            case 2:  # less
                self.set_brightness(int(self.get_properties()["current_brightness"]) - brightness_score)

    def set_alarm(self, time):
        self.alarm_mode = 1
        flow_alarm = flows.disco(120)
        self.start_flow(flow_alarm)

    def remove_alarm(self):
        #if self.alarm_mode == 1:
            self.alarm_mode = 0
            self.stop_flow()
            self.set_color_temp(6500)
