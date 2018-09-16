# loca2

TODO:

- Recycler-endlayout-handler instead of waiting before restoring state in quiz.
- blink correct when incorrect pair-it answer
- more padding with zoom-toggle-button
- fade out colors of roads etc, so not confused with drawn geo-objects
- replace alert-menu in exercise-activity with "speech bubble"
- change exercise name, color, and display exercise's creation data (important since OSM data is perishable).
- not white background of talking/fading-screen
- progress-bar during exercise creation (downloading, processing..)
- smarter reminder-counts/ reminder-object-selection
- prefer next level objects in pair-it (not for reminders..)
- smart color coding?
- conform polylines after map.. (functionality of mapbox?)

- switch overpass endpoint if problem.
- more restrictive tag-filtration

- index db?
- exercise-construction performance (load/insert/update/delete in chunks/trnsactions)
- toString() reflection
- trust bad overpass.certificates
- stats from pair-it

- Predefined exercise-areas, progress sharing
- Change theme/ map-theme
- Exercise persistance through app-uninstalls


KNOWN BUGS:
- Loading exercise service runns, activity killed. Start activity and stop service: not working
 (service reads exit-flag in previous instance).
- Exit-button in exercise-loading.. buggy.
- Sometimes false-positives in line-segment-crossing-detection during exercise creation.

(fixed?) - Exercise activity briefly started before follow-up quiz if tapping the screen during text So...




-översätt..:
leisure|fritid
lodging|logi
cultivation|odling
...
