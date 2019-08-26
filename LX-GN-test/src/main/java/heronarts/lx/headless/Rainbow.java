
package heronarts.lx.headless;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
// import heronarts.lx.model.LXModel;
import heronarts.lx.color.LXColor;



 // @LXCategory("Vibrant-Labs")
public class Rainbow extends LXPattern {

  public StairModel _stairModel;
  public Rainbow(LX lx, StairModel stairModel) {
    super(lx);
    _stairModel = stairModel;
  }

  public final float map(float value,
                                float start1, float stop1,
                                float start2, float stop2) {
    float outgoing =
      start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
    String badness = null;
    if (outgoing != outgoing) {
      badness = "NaN (not a number)";

    } else if (outgoing == Float.NEGATIVE_INFINITY ||
               outgoing == Float.POSITIVE_INFINITY) {
      badness = "infinity";
    }

    return outgoing;
  }


   @Override
   public void run(double deltaMs) {
     float n = 0;
     for(int i = 0; i < _stairModel.stairs.length; i ++){

       int stairLength = _stairModel.stairs[i].size;
       for (int j = 0; j < stairLength; j++) {

         LXPoint p = _stairModel.stairs[i].points[j];
         float hue = map(j, 0, stairLength, 0, 360);
         colors[p.index] = LXColor.hsb(hue, 100, 100);

       }
     }//end of stairs[i] iteration
   }
 }
