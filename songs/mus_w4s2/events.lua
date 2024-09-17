function event(event)
    if event == 1 then
        playSFX("snd_firework")
    elseif event == 3 or event == 7 then
        camFade(0, "#000000")
    elseif event == 4 or event == 8 or event == 13 or event == 14 then
        camFlash(1, "#ffffff")
    elseif event == 12 or event == 15 then
        camFade(1, "#ffffff")
    end
end