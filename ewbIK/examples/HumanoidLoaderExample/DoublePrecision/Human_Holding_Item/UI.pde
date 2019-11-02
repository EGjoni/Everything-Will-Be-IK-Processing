class UI {
PGraphics display, stencil;
PShader blurshader;
public boolean multipass = false; 
String pathUp = ".."+delim+".."+delim+".."+delim+".."+delim+"library"+delim;


public UI(boolean multipassAllowed) {
  currentDrawSurface = g; 
  if(multipassAllowed) {
    stencil = createGraphics(width, height, P3D);
    display = createGraphics(width, height, P3D);
    stencil.noSmooth();
    display.smooth(8);
    blurshader = loadShader(pathUp+"shaders"+delim+"blur-sep.glsl");
    blurshader.set("blurSize", 20);
    blurshader.set("sigma", 9f);
    multipass = true;        
  }
  dKusudama.kusudamaShader = loadShader(pathUp+"shaders"+delim+"kusudama.glsl", pathUp+"shaders"+delim+"kusudama_vert.glsl");
  dKusudama.kusudamaStencil = loadShader(pathUp+"shaders"+delim+"kusudama_stencil.glsl", pathUp+"shaders"+delim+"kusudama_vert.glsl");


}  

public void drawBoneInfo(PGraphics pg, dBone bone, int idx) {
  if(bone.isPinned()) {
    pg.strokeWeight(10); 
    pg.stroke(255,0,0); 
    point(pg, bone.getPinLocation());
  }

  String boneAngles = "";
  try {
    double[] angleArr = bone.getXYZAngle();
    boneAngles += " D ( " + degrees((float)angleArr[0]) + ",   " + degrees((float)angleArr[1]) + ",   " + degrees((float)angleArr[2]) + "  )";
    pg.fill(0);
    pg.text(boneAngles, (-width/2) +10,  (-height/2) + (10*idx)); 
  } catch (Exception e) {
  }        
}


public void drawInstructions(PGraphics pg, String append, float zoomScalar) {
  String appended = append == null ? "" : "-"+append;

  String instructionText =
      "-Click and drag to move the selected pin.\n"
          +"-To select a different pin, use the Up and Down arrows.\n"
          + "-Use the mouse wheel to rotate the pin about its (red) Y axis.\n" 
          + "-Hold shift while using the mouse wheel to rotate the pin about its (blue) Z axis.\n"
          + "-Hold ctrl while using the mouse wheel to rotate the pin about its (green) X axis. \n" 
          + appended;
  //pg.textMode(MODEL);
  if(pg.textFont == null)
    pg.textSize(12);
  else {
    pg.textSize(12);
  }
  //PFont instructionFont = createFont(pg.textFont.getName(), 42);
  pg.fill(0,0,0, 90);
  float boxW = pg.textWidth(instructionText); 
  float boxH = (pg.textAscent() + pg.textDescent()) * (instructionText.split("\n").length);
  pg.rect((-width/2f)+30, (-height/2f)+15, boxW+25, boxH+40);
  pg.fill(255, 255, 255, 255);
  pg.emissive(255, 255, 255);
  pg.text(instructionText, (-width/2f) + 50f, -height/2f + 40f);

}

public void printXY(PGraphics pg, PVector p) {
  System.out.println(pg.screenX(p.x, p.y, p.z)
      +", " + pg.screenY(p.x, p.y, p.z));
}
public void line(PGraphics pg, PVector p1, PVector p2) {
  pg.line(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
}

public void point(PGraphics pg, PVector p) {
  pg.point(p.x, p.y, p.z);
}
public void printXY(PGraphics pg, DVector pd) {
  PVector p = pd.toPVec();
  System.out.println(pg.screenX(p.x, p.y, p.z)
      +", " + pg.screenY(p.x, p.y, p.z));
}
public void line(PGraphics pg, DVector p1, DVector p2) {
  PVector p1f = p1.toPVec();
  PVector p2f = p2.toPVec(); 
  pg.line(p1f.x, p1f.y, p1f.z, p2f.x, p2f.y, p2f.z);
}

public void point(PGraphics pg, DVector pd) {
  PVector p = pd.toPVec();
  pg.point(p.x, p.y, p.z);
}  

public void drawPins(PGraphics pg, dIKPin activePin, 
    float zoomScalar, float drawSize,
    boolean cubeMode, dAxes cubeAxes) {

  if(activePin != null) {
    dAxes ellipseAx = cubeMode ? cubeAxes : (dAxes) activePin.getAxes();
    PVector pinLoc =  screenOf(pg, ellipseAx.origin_(), zoomScalar) ;
    PVector pinX = screenOf(pg, ellipseAx.x_().getScaledTo(30f), zoomScalar);
    PVector pinY = screenOf(pg, ellipseAx.y_().getScaledTo(30f), zoomScalar);
    PVector pinZ = screenOf(pg, ellipseAx.z_().getScaledTo(30f), zoomScalar);
    pg.fill(255,255,255, 150);
    pg.stroke(255, 0, 255);
    float totalpriorities = (float)(activePin.getXPriority() + activePin.getYPriority() + activePin.getZPriority()); 
    pg.ellipse(pinLoc.x, pinLoc.y, zoomScalar*50, zoomScalar*50);

    PVector effectorO = screenOf(pg, activePin.forBone().localAxes().origin_(), zoomScalar);
    PVector effectorX = screenOf(pg, activePin.forBone().localAxes().x_().getScaledTo(30f), zoomScalar);
    PVector effectorY = screenOf(pg, activePin.forBone().localAxes().y_().getScaledTo(30f), zoomScalar);
    PVector effectorZ = screenOf(pg, activePin.forBone().localAxes().z_().getScaledTo(30f), zoomScalar);
    pg.stroke(255,255,255,150);

    if(!cubeMode) {
      float xPriority = (float) activePin.getXPriority();
      float yPriority = (float) activePin.getYPriority();
      float zPriority = (float) activePin.getZPriority();
      drawPinEffectorHints(
          pg,
          pinLoc, 
          pinX, pinY, pinZ, 
          effectorO, 
          effectorX, effectorY, effectorZ,
          xPriority, yPriority, zPriority, totalpriorities
          );
    }
  }
}


public void drawPinEffectorHints(PGraphics pg, 
    PVector pinLoc, 
    PVector pinX, PVector pinY, PVector pinZ, 
    PVector effectorO, 
    PVector effectorX, PVector effectorY, PVector effectorZ,
    float xPriority, float yPriority, float zPriority, float totalpriorities) {

  pg.line(pinLoc.x, pinLoc.y, pinLoc.z, effectorO.x, effectorO.y, effectorO.z);
  pg.stroke(0,255,0,150);
  pg.strokeWeight(2f*xPriority / totalpriorities);
  pg.line(pinX.x, pinX.y, pinX.z,  effectorX.x, effectorX.y, effectorX.z);
  pg.stroke(255,0,0,150);
  pg.strokeWeight(2f*yPriority/ totalpriorities);
  pg.line(pinY.x, pinY.y, pinY.z, effectorY.x, effectorY.y, effectorY.z);
  pg.stroke(0, 0,255,150);
  pg.strokeWeight(2f*zPriority / totalpriorities);
  pg.line(pinZ.x, pinZ.y, pinZ.z, effectorZ.x, effectorZ.y, effectorZ.z);
}


public void drawPass(int mode, float drawSize, Runnable preArmatureDraw, PGraphics buffer, dArmature armature) {
  dKusudama.renderMode = mode;
  dBone.renderMode = mode;
  dAxes.renderMode = mode;
  if(preArmatureDraw != null)
    preArmatureDraw.run();
  armature.drawMe( buffer, 100, drawSize);
}  

public  PVector screenOf(PGraphics pg, PVector pt, float zoomScalar) {
  return new PVector(
      (pg.screenX((float)pt.x, (float)pt.y, (float)pt.z)*zoomScalar) - orthoWidth/2f,
      (pg.screenY((float)pt.x, (float)pt.y, (float)pt.z)*zoomScalar)- orthoHeight/2f);
}

public<V extends Vec3d<?>>  PVector screenOf(PGraphics pg, V pt, float zoomScalar) {
  return new PVector(
      (pg.screenX((float)pt.x, (float)pt.y, (float)pt.z)*zoomScalar) - orthoWidth/2f,
      (pg.screenY((float)pt.x, (float)pt.y, (float)pt.z)*zoomScalar)- orthoHeight/2f);
}


private PGraphics currentDrawSurface = null;

public void drawScene(float zoomScalar, float drawSize,
    Runnable additionalDraw, 
    dArmature armature, 
    String usageInstructions,
    dIKPin activePin, dAxes cubeAxes, boolean cubeEnabled) {
  if(multipass) {
    dKusudama.enableMultiPass(true);
    currentDrawSurface = stencil;

    stencil.beginDraw();
    setCamera(stencil, zoomScalar);
    drawPass(0, drawSize, additionalDraw, stencil, armature);  
    stencil.endDraw();

    currentDrawSurface = display;
    display.beginDraw();
    setSceneAndCamera(display, zoomScalar);
    drawPass(1, drawSize, additionalDraw, display, armature);    
    blurshader.set("mask", stencil);
    blurshader.set("horizontalPass", 0); display.filter(blurshader);
    blurshader.set("horizontalPass", 1);  display.filter(blurshader);      
    display.endDraw();

    currentDrawSurface = g;
    setCamera(g, zoomScalar);
    background(80, 150, 190);
    imageMode(CENTER);
    image(display, 0, 0, orthoWidth, orthoHeight);      
    resetMatrix();
    drawPins(g, activePin, zoomScalar, drawSize, cubeEnabled, cubeAxes);
    resetMatrix();  float cx =width;   float cy =height;
    ortho(-cx/2f, cx/2f,  -cy/2f, cy/2f, -1000, 1000);
    drawInstructions(g, usageInstructions, zoomScalar); 
    drawPins(g, activePin, drawSize, zoomScalar, cubeEnabled, cubeAxes);
    drawInstructions(g, usageInstructions, zoomScalar); 
  }  else {      
    dKusudama.enableMultiPass(false);
    currentDrawSurface = g;
    setSceneAndCamera(g, zoomScalar);
    background(80, 150, 190);
    drawPass(1, drawSize, additionalDraw, g, armature);
    resetMatrix();
    drawPins(g, activePin, zoomScalar, drawSize, cubeEnabled, cubeAxes);
    resetMatrix();  float cx =width;   float cy =height;
    ortho(-cx/2f, cx/2f,  -cy/2f, cy/2f, -1000, 1000);
    drawInstructions(g, usageInstructions, zoomScalar); 
  }
}

PVector mouse = new PVector(0,0,0);        
PVector cameraPosition = new PVector(0, 0, 70); 
PVector lookAt = new PVector(0, 0, 0);
PVector up = new PVector(0, 1, 0);


public void toggleMultipass() {
  multipass = !multipass;
  if(stencil == null && multipass) {
    stencil = createGraphics(1200, 900, P3D);
    display = createGraphics(1200, 900, P3D);
    stencil.noSmooth();
    display.smooth(8);
    blurshader = loadShader( "src/ewbik/processing/blur-sep.glsl");
    blurshader.set("blurSize", 20);
    blurshader.set("sigma", 9f);
  }
}

public void camera(PVector cp, PVector so, PVector up, PGraphics pg) {
  pg.camera(cp.x, cp.y, cp.z, so.x, so.y, so.z, up.x, up.y, up.z);
}


public void setSceneAndCamera(PGraphics pg, float zoomScalar) {
  setCamera(pg, zoomScalar);    
  pg.directionalLight(148, 148, 148, 0, 100, 100);
  pg.directionalLight(148, 148, 148, 0, 100, -100);
  pg.directionalLight(148, 148, 148, 100, 100, 0);
  pg.directionalLight(148, 148, 148, -100, 100, 0);
  pg.directionalLight(48, 48, 48, 100, -10, 100);
  pg.directionalLight(48, 48, 48, 100, -10, -100);
  pg.directionalLight(48, 48, 48, -100, -10, 100);
  pg.directionalLight(48, 48, 48, -100, -10, -100);  
}

float orthoHeight, orthoWidth;

public void setCamera(PGraphics pg, float zoomScalar) {
  pg.clear();
  orthoHeight = height*zoomScalar;
  orthoWidth = ((float)width/(float)height) * orthoHeight; 
  mouse.x =  (mouseX - (width/2f)) * (orthoWidth/width); mouse.y = (mouseY - (height/2f)) *  (orthoHeight/height);
  camera(cameraPosition, lookAt, up, pg);
  pg.ortho(-orthoWidth/2f, orthoWidth/2f, -orthoHeight/2f, orthoHeight/2f, -1000, 1000); 
}

/**
 * @return the draw surface this class is currently operating on. 
 * This is used as kind of a hack so I don't have to bother writing interfaces  just to render a box when using multipass. 
 */
public PGraphics getCurrentDrawSurface() {
  return currentDrawSurface;
}
}
