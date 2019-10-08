# Everything-Will-Be-IK-Processing

This project serves as both a Processing extension of the robust java Inverse Kinematics library <a href="https://github.com/EGjoni/Everything-Will-Be-IK">Everything Will Be IK</a>, and an official reference for anyone looking to port or extend EWBIK for their own purposes. 


See here for a demo: https://youtu.be/y_o34kOx_FA
</br></br>
<b>Features:</b>
<ul>
<li>Orientation AND position based targets (6-DOF).</li>
<li>Highly stable.</li>
<li>Multiple end-effector support</li>
<li>Intermediary effector support.</li>
<li>Dampening (stiffness control).</li>
<li>Target weight/priority (per target, per degree of freedom).</li>
<li>Highly versatile 3-DOF constraints with arbitrarily shaped orientation regions.</li>
</ul>
</br>


<b>Installation Instructions:</b></br>
<ol>
<li>Download a .zip from this repository.</li>
<li>Locate your processing sketchbook folder. (You can find this from within the Processing IDE by clicking File -> Preferences).</li>
<li>Navigate to that sketchbook directory.</li>
<li>Extract the ewbIK folder into the 'libraries' folder within your sketchbook.</li>
</ol>
The final directory layout should look something like

```
..sketchbook/
  ┣ libraries/
    ┣ ewbIK/
      ┣ doc/
      ┣ examples/
      ┣ library/
      ┣ src/
```


<b>DISCLAIMER: This code was intended primarily for graphics applications and has not been thoroughly tested for use in robotics. Until this disclaimer disappears (or you have independently verified it is suitable for your purposes) please do not use this code to command any servos that can put out enough torque to cause damage to people, property, or other components in your build.</b>
