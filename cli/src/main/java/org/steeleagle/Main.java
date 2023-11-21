package org.steeleagle;

import kala.collection.immutable.ImmutableMap;
import kala.text.StringSlice;
import org.jetbrains.annotations.NotNull;
import org.steeleagle.concrete.AST;
import org.steeleagle.concrete.Preparse;
import org.steeleagle.concrete.Task;
import org.steeleagle.psi.DslParserImpl;
import org.steeleagle.psi.StreamReporter;
import org.steeleagle.pythonGen.CodeGeneratorPython;

import static org.steeleagle.parser.BotPsiElementTypes.*;

public class Main {
  public static void main(String[] args) {
    var node = parser().parseNode("""
        Task {
            Detect task1 {
                way_points: [(-80.0076661, 40.4534506, 15.0), (-80.0075856, 40.4526669, 15.0), (-80.0061211, 40.4526995, 15.0), (-80.0057885, 40.4536384, 15.0), (-80.0076661, 40.4534506, 15.0)],
                gimbal_pitch: -45.0,
                drone_rotation: 0.0,
                sample_rate: 2,
                hover_delay: 5,
                model: coco
            }
        }
        Mission {
            Start { task1 }
            Transition (timeout(5)) task1 -> task2
        }
        """);
    System.out.println(node.toDebugString());

    ImmutableMap<String, Task> taskMap = ImmutableMap.from(node.child(TASK).childrenOfType(TASK_DECL).map(Preparse::createTask));

    var startTaskID = Preparse.createMission(node.child(MISSION).child(MISSION_CONTENT), taskMap);
    var ast = new AST(startTaskID, taskMap);

    CodeGeneratorPython.generateCode(ast);
    // System.out.println(node.toDebugString());
  }

  @NotNull
  private static DslParserImpl parser() {
    return new DslParserImpl(new StreamReporter(System.out));
  }
}
