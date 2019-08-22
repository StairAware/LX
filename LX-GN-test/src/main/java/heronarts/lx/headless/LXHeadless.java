/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
 *
 * This file is part of the LX Studio software library. By using
 * LX, you agree to the terms of the LX Studio Software License
 * and Distribution Agreement, available at: http://lx.studio/license
 *
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 *
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 *
 */

package heronarts.lx.headless;

import java.io.File;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.GridModel;
import heronarts.lx.model.LXModel;
import heronarts.lx.output.ArtNetDatagram;
import heronarts.lx.output.FadecandyOutput;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.OPCOutput;


/**
 * Example headless CLI for the LX engine. Just write a bit of scaffolding code
 * to load your model, define your outputs, then we're off to the races.
 */
public class LXHeadless {

  // public static LXModel buildModel() {
  //   // TODO: implement code that loads and builds your model here
  //   return new GridModel(30, 30);
  // }

  public static class StairModel extends LXModel {

    public final static int NUM_STAIRS = 2;
    public final static int STAIR_HEIGHT = 20;
    public final static int STAIR_DEPTH = 30;
    public final static int LEDS_PER_STAIR = 53;
    public final static float LED_SPACING = 1;
    public final static float XOFFSET = 0.5;
    public final static float YOFFSET = 0;
    public final static float ZOFFSET = 0.1;
    public final static float SENSOR_SPACING = (131/25.4)*2; // The sensors are 131mm apart (converted to inches)



    public final StripModel[] stairs;

    public final int[] numSens ={9, 9};

    public StairModel() {
      super(makeStairs());
      this.stairs = this.fixtures.toArray(new StripModel[0]);
    }

    public static StripModel[] makeStairs() {
      StripModel[] stairs = new StripModel[NUM_STAIRS];
      for (int i = 0; i < NUM_STAIRS; ++i) {
        StripModel.Metrics metrics = new StripModel.Metrics(LEDS_PER_STAIR)
          .setOrigin(-LEDS_PER_STAIR*LED_SPACING/2+XOFFSET, (i)*STAIR_HEIGHT+STAIR_HEIGHT/2, (i)*STAIR_DEPTH-STAIR_DEPTH/2-ZOFFSET )
          .setSpacing(LED_SPACING, 0, 0);
        stairs[i] = new StripModel(metrics);
      }
      return stairs;
    }

    //public float getSensorSpacing() {
    //    return LED_SPACING;
    //  }

    public static class Fixture extends LXAbstractFixture {
      Fixture() {
        for (int i = 0; i < NUM_STAIRS; ++i) {
        }
      }
    }
  }
  // public static void addArtNetOutput(LX lx) throws Exception {
  //   lx.engine.addOutput(
  //     new LXDatagramOutput(lx).addDatagram(
  //       new ArtNetDatagram(lx.model, 512, 0)
  //       .setAddress("localhost")
  //     )
  //   );
  // }
  //
  // public static void addFadeCandyOutput(LX lx) throws Exception {
  //   lx.engine.addOutput(new FadecandyOutput(lx, "localhost", 9090, lx.model));
  // }
  //
  // public static void addOPCOutput(LX lx) throws Exception {
  //   lx.engine.addOutput(new OPCOutput(lx, "localhost", 7890));
  // }




  public static void main(String[] args) {
    try {
      LXModel model = buildModel();
      LX lx = new LX(model);

      try {
        lx.engine.osc.receiver(9000).addListener(new LXOscListener() {
          public void oscMessage(OscMessage message) {
          // Receive data from brain about stair sensor positions
          // You can store that here somewhere, or update parameters, etc., so that patterns can refer to it
          //println("stairAware received osc message:"+message);
          String addr = message.getAddressPattern().getValue();
          //get the stair info, such as /stair0
          if(addr.substring(0, 6).equals("/stair")) {
            newOSC = true;
            oscData.stairNumber = parseInt(addr.substring(6));
            oscData.sensorIndex = message.getInt(0);
            oscData.sensorValue = message.getInt(1);
            //println(oscData.stairNumber,oscData.sensorIndex, oscData.sensorValue);

            //make sure that the sensor index is matched to our model
            if(oscData.stairNumber<model.NUM_STAIRS && oscData.sensorIndex < model.numSens[oscData.stairNumber]){
              sensors.add(new ActiveSensor(oscData.stairNumber, oscData.sensorIndex, oscData.sensorValue));
            }
          }
        }
      });
    } catch (java.net.SocketException sx) {
      sx.printStackTrace();
    }


    try {
      // In this example, we send an artnet packet for each stair, with its own universe number
      final String ARTNET_IP = "192.168.10.49";
      LXDatagramOutput output = new LXDatagramOutput(lx);
      int stairIndex = 0;
      for (StripModel stair : model.stairs) {
        // Each stair sends an ArtNet datagram to the IP address
        output.addDatagram(
          new ArtNetDatagram(stair)
          .setUniverseNumber(stairIndex)
          .setAddress(ARTNET_IP)
        );
        ++stairIndex;
      }
      // Add the output to the engine
      lx.addOutput(output);

    } catch (Exception x) {
      println("Error in ArtNet output mapping!");
      x.printStackTrace();
    }

      // On the CLI you may specify an argument with an .lxp file
      if (args.length > 0) {
        lx.openProject(new File(args[0]));
      } else {
        lx.setPatterns(new LXPattern[] {
          new ExamplePattern(lx)
        });
      }

      lx.engine.start();
    } catch (Exception x) {
      System.err.println(x.getLocalizedMessage());
    }
  }
}



public static class OscData {
  int stairNumber;
  int sensorIndex;
  int sensorValue;
}

public class ActiveSensor {
  int stairNum;
  int sensorIndex;
  int sensorValue;
  float sensorPos;

  ActiveSensor(int sn, int si, int sv) {
    stairNum = sn;
    sensorIndex = si;
    sensorValue = sv;
    calculateSensorPos();
  }

  void resetSensorValue() {
    if (sensorValue>0) {
      sensorValue*=0.85;
    }
  }

  //get the sensor position by adding all previous stair length plus the sensorIndex*SENSOR_SPACING of current sSENSORVOIDCculateSensorPos() {
  void calculateSensorPos(){
    //sensorPos = 0;
    //for (int i = 0; i < stairNum; i ++) {
    //  sensorPos += model.LEDS_PER_STAIR*model.LED_SPACING;
    //}
    //sensorPos += sensorIndex*model.SENSOR_SPACING;
    sensorPos = (sensorIndex+1)*model.SENSOR_SPACING;
  }
}


// Configuration flags
final static boolean MULTITHREADED = true;
final static boolean RESIZABLE = true;

// Helpful global constants
final static float INCHES = 1;
final static float IN = INCHES;
final static float FEET = 12 * INCHES;
final static float FT = FEET;
final static float CM = IN / 2.54;
final static float MM = CM * .1;
final static float M = CM * 100;
final static float METER = M;
