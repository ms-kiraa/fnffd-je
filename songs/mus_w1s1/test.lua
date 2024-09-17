local pos = {}
local possy = {}

function onCreate()
    print("hel yeah")
    for i = 0,3 do
        pos[i] = getNoteY("Player", i)
        possy[i] = getNoteX("Player", i)-200
        print("setting visiblity "..i)
        setNoteVisibility("BadGuy", i, false)
    end
end

function onUpdate()
    for i = 0,3 do
        if possy[i] ~= nil and pos[i] ~= nil and getStepDb() ~= nil then
            setNotePosition("Player", i,
            possy[i] +
            (math.cos(getStepDb()+i)*25),
            pos[i] +
            (math.sin(getStepDb()+i)*25))
        end
    end
end

function onStepHit(curStep)
    if curStep == 4 then
        setCamTarget(0, 0)
    end
end