local mod = 1 --????

function onCreate()
    makeSolidLuaObject("blanker", "000000", screenWidth, screenHeight, 0, 0)
    addLuaObject("blanker", "UI")
    setLuaObjectScrollFactor("blanker", 0, 0)

    makeImage("silly", "tempstarfire/0")
    --[[setLuaObjectScrollFactor("silly", 0, 0)
    scaleLuaObject("silly", 2)
    centerLuaObject("silly")
    applyDudeSkinToObject("silly")
    addLuaObject("silly", "UI")]]
    applyDudeSkinToImage("silly")
end

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

    -- specific event code
    if event == 13 then
        removeLuaObject("blanker")
    end
end