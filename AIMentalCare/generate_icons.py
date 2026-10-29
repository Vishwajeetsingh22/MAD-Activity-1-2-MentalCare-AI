import os
from PIL import Image, ImageDraw

res_dir = r"AIMentalCareApp\app\src\main\res"

# 1. Base assets already perfected:
# - transparent_squircle.png: 512x512 squircle emblem on genuine transparent background
# - white_symbol_perfect.png: 512x512 white brain-leaf symbol on genuine transparent background
squircle = Image.open("transparent_squircle.png").convert("RGBA")
symbol = Image.open("white_symbol_perfect.png").convert("RGBA")

# Verify squircle has transparent background outside
sq_arr = list(squircle.getdata())
corner_alpha = [sq_arr[0][3], sq_arr[511][3], sq_arr[512*511][3], sq_arr[512*512-1][3]]
print("Squircle corners alpha:", corner_alpha)

# Save drawable/ic_logo.png
logo_path = os.path.join(res_dir, "drawable", "ic_logo.png")
squircle.save(logo_path, format="PNG")
print(f"Saved {logo_path} (size={squircle.size})")

# 2. Build circular emblem for legacy ic_launcher_round:
# Create a circle version of the dark green #047857 background + white symbol
round_emblem = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
circle_bg = Image.new("RGBA", (512, 512), (4, 120, 87, 255)) # Dark green #047857

# Extract white symbol cropped
bbox = symbol.getbbox()
sym_crop = symbol.crop(bbox)
target_h = 320
target_w = int(target_h * (sym_crop.width / sym_crop.height))
sym_resized = sym_crop.resize((target_w, target_h), Image.Resampling.LANCZOS)

# Paste white symbol onto circle background
sym_pos = ((512 - target_w) // 2, (512 - target_h) // 2)
circle_bg.paste(sym_resized, sym_pos, sym_resized)

# Mask to circle
mask_circle = Image.new("L", (512, 512), 0)
draw = ImageDraw.Draw(mask_circle)
draw.ellipse((16, 16, 496, 496), fill=255)
round_emblem.paste(circle_bg, (0, 0), mask_circle)
round_emblem.save("legacy_round_master.png")
print("Saved legacy_round_master.png")

# 3. Create Adaptive Icon Foreground master:
# 108dp standard canvas, 72dp safe zone (~66.7%).
# Master size: 432 x 432 px
fg_master = Image.new("RGBA", (432, 432), (0, 0, 0, 0))
fg_sym_h = 240 # ~55.5% of 432, safely inside the 288px safe zone (no clipping under any mask)
fg_sym_w = int(fg_sym_h * (sym_crop.width / sym_crop.height))
fg_sym_resized = sym_crop.resize((fg_sym_w, fg_sym_h), Image.Resampling.LANCZOS)
fg_master.paste(fg_sym_resized, ((432 - fg_sym_w) // 2, (432 - fg_sym_h) // 2), fg_sym_resized)
fg_master.save("adaptive_foreground_master.png")
print("Saved adaptive_foreground_master.png")

# 4. Generate all densities:
# Mipmap densities
densities = {
    "mipmap-mdpi": {"launcher": 48, "fg": 108},
    "mipmap-hdpi": {"launcher": 72, "fg": 162},
    "mipmap-xhdpi": {"launcher": 96, "fg": 216},
    "mipmap-xxhdpi": {"launcher": 144, "fg": 324},
    "mipmap-xxxhdpi": {"launcher": 192, "fg": 432}
}

for folder, sizes in densities.items():
    folder_path = os.path.join(res_dir, folder)
    os.makedirs(folder_path, exist_ok=True)
    
    # ic_launcher.png (legacy squircle)
    l_size = sizes["launcher"]
    sq_res = squircle.resize((l_size, l_size), Image.Resampling.LANCZOS)
    sq_res.save(os.path.join(folder_path, "ic_launcher.png"), format="PNG")
    
    # ic_launcher_round.png (legacy circle)
    rnd_res = round_emblem.resize((l_size, l_size), Image.Resampling.LANCZOS)
    rnd_res.save(os.path.join(folder_path, "ic_launcher_round.png"), format="PNG")
    
    # ic_launcher_foreground.png (adaptive foreground)
    fg_size = sizes["fg"]
    fg_res = fg_master.resize((fg_size, fg_size), Image.Resampling.LANCZOS)
    fg_res.save(os.path.join(folder_path, "ic_launcher_foreground.png"), format="PNG")
    
    print(f"Generated {folder}: ic_launcher ({l_size}px), ic_launcher_round ({l_size}px), ic_launcher_foreground ({fg_size}px)")

# 5. Create mipmap-anydpi-v26 XML files
v26_folder = os.path.join(res_dir, "mipmap-anydpi-v26")
os.makedirs(v26_folder, exist_ok=True)

xml_content = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
"""

with open(os.path.join(v26_folder, "ic_launcher.xml"), "w", encoding="utf-8") as f:
    f.write(xml_content)
with open(os.path.join(v26_folder, "ic_launcher_round.xml"), "w", encoding="utf-8") as f:
    f.write(xml_content)

print("Generated mipmap-anydpi-v26/ic_launcher.xml and ic_launcher_round.xml")
