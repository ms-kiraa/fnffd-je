function onCreatePre()
    makeLuaObject("back2", "houseback2", -35, 30)
    setLuaObjectScrollFactor("back2", 0.7)
    addLuaObject("back2")

    makeLuaObject("back1", "houseback1", 0, 0)
    addLuaObject("back1")

    setBackground(145,207,221)
end

function onCreate()
    setCharacterPosition("dude", 525, 290)

    setCharacterPosition("badguy", 260, 290)
end