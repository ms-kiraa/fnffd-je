local mod = 1 --????

function event(event)
    if event == 0+mod then
        playSFX("snd_firework")
    elseif event == 2+mod or event == 6+mod then
        camFade(0, "#000000")
    elseif event == 3+mod or event == 7+mod or event == 12+mod or event == 13+mod or event == 15+mod then
        camFlash(1, "#ffffff")
    elseif event == 11+mod or event == 14+mod then
        camFade(1, "#ffffff")
    end
end