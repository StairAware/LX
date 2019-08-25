
package heronarts.lx.headless;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.color.LXColor;

public class PlanePattern extends LXPattern {

  private final LXModulator hue = startModulator(new SawLFO(0, 360, 9000));
  private final LXModulator brightness = startModulator(new SinLFO(10, 100, 4000));
  private final LXModulator xPos = startModulator(new SinLFO(0, 1, 5000));
  private final LXModulator width = startModulator(new SinLFO(.4, 1, 3000));


  public enum Axis {
    X, Y, Z
  }

  public float pos = 0.5f;
  public float wth = 0.4f;

  public PlanePattern(LX lx) {
    super(lx);
    // addParameter("axis", this.axis);
    // addParameter("pos", this.pos);
    // addParameter("width", this.wth);
  }


   @Override
   public void run(double deltaMs) {
     float pos = xPos.getValuef();
     float falloff = 100 / this.wth;
     float n = 0;
     for (LXPoint p : model.points) {
       switch ("Y") {
       case "X": n = p.xn; break;
       case "Y": n = p.yn; break;
       case "Z": n = p.zn; break;
       }
       colors[p.index] = LXColor.gray(Math.max(0, 100 - falloff*Math.abs(n - pos)));
     }
   }
 }


// // Here is a fairly basic example pattern that renders a plane that can be moved
// // across one of the axes.
// @LXCategory("Form")
// public static class PlanePattern extends LXPattern {
//
//  public enum Axis {
//    X, Y, Z
//  };
//
//  public final EnumParameter<Axis> axis =
//    new EnumParameter<Axis>("Axis", Axis.X)
//    .setDescription("Which axis the plane is drawn across");
//
//  // public final CompoundParameter pos = new CompoundParameter("Pos", 0, 1)
//  //   .setDescription("Position of the center of the plane");
//  //
//  // public final CompoundParameter wth = new CompoundParameter("Width", .4, 0, 1)
//  //   .setDescription("Thickness of the plane");
//
//  float pos = 0.5;
//  float wth = 0.4;
//
//  public PlanePattern(LX lx) {
//    super(lx);
//    // addParameter("axis", this.axis);
//    // addParameter("pos", this.pos);
//    // addParameter("width", this.wth);
//  }
//
//  public void run(double deltaMs) {
//    float pos = this.pos.getValuef();
//    float falloff = 100 / this.wth.getValuef();
//    float n = 0;
//    for (LXPoint p : model.points) {
//      switch (this.axis.getEnum()) {
//      case X: n = p.xn; break;
//      case Y: n = p.yn; break;
//      case Z: n = p.zn; break;
//      }
//      colors[p.index] = LXColor.gray(max(0, 100 - falloff*abs(n - pos)));
//    }
//  }
// }
