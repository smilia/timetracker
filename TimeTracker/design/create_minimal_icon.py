from PIL import Image, ImageDraw
import math

# Create a high-resolution icon (1024x1024 for app store)
size = 1024
img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Minimal color palette - only 2 colors
bg_color = (255, 255, 255)  # Pure white
accent_color = (0, 122, 255)  # iOS blue

# Background with rounded corners
corner_radius = 200
draw.rounded_rectangle([(0, 0), (size, size)], radius=corner_radius, fill=bg_color)

# Center point
center_x, center_y = size // 2, size // 2

# Draw a simple, elegant clock
clock_radius = 280

# Clock circle - thin line
draw.ellipse([
    center_x - clock_radius,
    center_y - clock_radius,
    center_x + clock_radius,
    center_y + clock_radius
], outline=accent_color, width=12)

# Hour markers - minimal dots
for i in range(12):
    angle = math.radians(i * 30)
    marker_distance = clock_radius - 60
    mx = center_x + marker_distance * math.cos(angle - math.pi/2)
    my = center_y + marker_distance * math.sin(angle - math.pi/2)
    
    # Only show 12, 3, 6, 9
    if i % 3 == 0:
        draw.ellipse([
            mx - 16, my - 16,
            mx + 16, my + 16
        ], fill=accent_color)

# Hour hand - pointing to 10 (simple line)
hour_angle = math.radians(300)  # 10 o'clock
hour_length = 140
hour_x = center_x + hour_length * math.cos(hour_angle - math.pi/2)
hour_y = center_y + hour_length * math.sin(hour_angle - math.pi/2)
draw.line([(center_x, center_y), (hour_x, hour_y)], fill=accent_color, width=24)

# Minute hand - pointing to 2 (simple line)
minute_angle = math.radians(60)  # 2 o'clock
minute_length = 200
minute_x = center_x + minute_length * math.cos(minute_angle - math.pi/2)
minute_y = center_y + minute_length * math.sin(minute_angle - math.pi/2)
draw.line([(center_x, center_y), (minute_x, minute_y)], fill=accent_color, width=16)

# Center dot - solid
draw.ellipse([
    center_x - 24,
    center_y - 24,
    center_x + 24,
    center_y + 24
], fill=accent_color)

# Save main icon
img.save('f:\\时间记录app\\TimeTracker\\design\\app_icon_minimal_1024.png', 'PNG')

# Save different sizes
sizes = [512, 192, 144, 96, 72, 48]
for s in sizes:
    resized = img.resize((s, s), Image.Resampling.LANCZOS)
    resized.save(f'f:\\时间记录app\\TimeTracker\\design\\app_icon_minimal_{s}.png')

print("Minimal app icon created successfully!")
print("Files saved in design/ folder")
