mogrify -fuzz 42%% -transparent yellow *.png
mogrify -grayscale Rec709Luminance *.png
mogrify -negate *.png