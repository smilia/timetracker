from PIL import Image, ImageDraw
import math

# Create a high-resolution icon (1024x1024 for app store)
size = 1024
img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Colors - sophisticated palette
bg_color = (250, 250, 252)  # Soft off-white
primary_color = (59, 130, 246)  # Refined blue
secondary_color = (99, 102, 241)  # Indigo accent
accent_color = (16, 185, 129)  # Productive green
shadow_color = (0, 0, 0, 30)  # Soft shadow

# Background with rounded corners
corner_radius = 200
bg_box = [(0, 0), (size, size)]
draw.rounded_rectangle(bg_box, radius=corner_radius, fill=bg_color)

# Create the time block composition
center_x, center_y = size // 2, size // 2
block_size = 140
gap = 20

# Shadow offset
shadow_offset = 8

# Define the block layout (3x3 grid with center empty for clock concept)
blocks = [
    # Top row
    {"x": -1, "y": -1, "color": primary_color, "alpha": 255},
    {"x": 0, "y": -1, "color": secondary_color, "alpha": 230},
    {"x": 1, "y": -1, "color": primary_color, "alpha": 200},
    # Middle row (center empty for clock)
    {"x": -1, "y": 0, "color": accent_color, "alpha": 240},
    # Center is empty - will have clock hands
    {"x": 1, "y": 0, "color": secondary_color, "alpha": 220},
    # Bottom row
    {"x": -1, "y": 1, "color": secondary_color, "alpha": 210},
    {"x": 0, "y": 1, "color": primary_color, "alpha": 235},
    {"x": 1, "y": 1, "color": accent_color, "alpha": 225},
]

# Draw blocks with shadows
for block in blocks:
    bx = center_x + block["x"] * (block_size + gap)
    by = center_y + block["y"] * (block_size + gap)
    
    # Shadow
    shadow_box = [
        bx - block_size//2 + shadow_offset,
        by - block_size//2 + shadow_offset,
        bx + block_size//2 + shadow_offset,
        by + block_size//2 + shadow_offset
    ]
    draw.rounded_rectangle(shadow_box, radius=24, fill=shadow_color)
    
    # Block
    color = block["color"] + (block["alpha"],)
    block_box = [
        bx - block_size//2,
        by - block_size//2,
        bx + block_size//2,
        by + block_size//2
    ]
    draw.rounded_rectangle(block_box, radius=24, fill=color)
    
    # Inner highlight for depth
    highlight_box = [
        bx - block_size//2 + 4,
        by - block_size//2 + 4,
        bx + block_size//2 - 4,
        by - block_size//2 + 20
    ]
    draw.rounded_rectangle(highlight_box, radius=16, fill=(255, 255, 255, 40))

# Draw clock in the center
clock_radius = 100
circle_box = [
    center_x - clock_radius,
    center_y - clock_radius,
    center_x + clock_radius,
    center_y + clock_radius
]

# Clock shadow
draw.ellipse([
    center_x - clock_radius + shadow_offset,
    center_y - clock_radius + shadow_offset,
    center_x + clock_radius + shadow_offset,
    center_y + clock_radius + shadow_offset
], fill=shadow_color)

# Clock face
draw.ellipse(circle_box, fill=(255, 255, 255, 250), outline=primary_color, width=6)

# Clock hands
# Hour hand (pointing to 10)
hour_angle = math.radians(300)  # 10 o'clock
hour_length = 50
hour_x = center_x + hour_length * math.cos(hour_angle - math.pi/2)
hour_y = center_y + hour_length * math.sin(hour_angle - math.pi/2)
draw.line([(center_x, center_y), (hour_x, hour_y)], fill=primary_color, width=12)

# Minute hand (pointing to 2)
minute_angle = math.radians(60)  # 2 o'clock
minute_length = 70
minute_x = center_x + minute_length * math.cos(minute_angle - math.pi/2)
minute_y = center_y + minute_length * math.sin(minute_angle - math.pi/2)
draw.line([(center_x, center_y), (minute_x, minute_y)], fill=secondary_color, width=8)

# Center dot
draw.ellipse([
    center_x - 12,
    center_y - 12,
    center_x + 12,
    center_y + 12
], fill=primary_color)

# Add subtle gradient overlay for depth
gradient = Image.new('RGBA', (size, size), (0, 0, 0, 0))
gradient_draw = ImageDraw.Draw(gradient)
for i in range(size):
    alpha = int(15 * (1 - i / size))  # Fade from top
    gradient_draw.line([(0, i), (size, i)], fill=(255, 255, 255, alpha))

img = Image.alpha_composite(img, gradient)

# Save main icon
img.save('f:\\时间记录app\\TimeTracker\\design\\app_icon_1024.png', 'PNG')

# Save different sizes to design folder
sizes = [512, 192, 144, 96, 72, 48]
for s in sizes:
    resized = img.resize((s, s), Image.Resampling.LANCZOS)
    resized.save(f'f:\\时间记录app\\TimeTracker\\design\\app_icon_{s}.png')

# Create foreground layer for adaptive icons (Android)
fg_size = 1024
fg_img = Image.new('RGBA', (fg_size, fg_size), (0, 0, 0, 0))
fg_draw = ImageDraw.Draw(fg_img)

# Draw just the blocks and clock without background
for block in blocks:
    bx = center_x + block["x"] * (block_size + gap)
    by = center_y + block["y"] * (block_size + gap)
    
    color = block["color"] + (block["alpha"],)
    block_box = [
        bx - block_size//2,
        by - block_size//2,
        bx + block_size//2,
        by + block_size//2
    ]
    fg_draw.rounded_rectangle(block_box, radius=24, fill=color)

# Clock
draw.ellipse(circle_box, fill=(255, 255, 255, 250), outline=primary_color, width=6)
draw.line([(center_x, center_y), (hour_x, hour_y)], fill=primary_color, width=12)
draw.line([(center_x, center_y), (minute_x, minute_y)], fill=secondary_color, width=8)
draw.ellipse([
    center_x - 12,
    center_y - 12,
    center_x + 12,
    center_y + 12
], fill=primary_color)

fg_img.save('f:\\时间记录app\\TimeTracker\\design\\app_icon_foreground.png', 'PNG')

print("App icon created successfully!")
print("Files saved:")
print("- design/app_icon_1024.png (Main icon)")
print("- design/app_icon_foreground.png (Foreground layer)")
