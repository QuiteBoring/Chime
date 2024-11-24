package chime.processor;

import chime.calculator.util.Node;
import chime.path.PathElm;
import chime.path.impl.FallNode;
import chime.path.impl.JumpNode;
import chime.path.impl.TravelNode;
import chime.processor.impl.FallProcessor;
import chime.processor.impl.JumpProcessor;
import chime.processor.impl.TravelProcessor;

import java.util.ArrayList;
import java.util.List;

public class ProcessorManager {

    public static List<PathElm> process(List<Node> nodes) {
        List<PathElm> pathElms = convertRepresentation(nodes);
        List<Processor> processors = new ArrayList<>();
        processors.add(new TravelProcessor());
        processors.add(new FallProcessor());
        processors.add(new JumpProcessor());

        for (Processor processor : processors) {
            processor.process(pathElms);
        }

        return pathElms;
    }

    private static List<PathElm> convertRepresentation(List<Node> nodes) {
        List<PathElm> pathElms = new ArrayList<>();

        for(Node node : nodes) {
            int index = nodes.indexOf(node);

            if (node.isJumpNode()) {
                pathElms.add(new JumpNode(node.getX(), node.getY(), node.getZ()));
                continue;
            }

            if (node.isFallNode()) {
                pathElms.add(new FallNode(node.getX(), node.getY(), node.getZ()));
                if (index != nodes.size() - 1) index += 1;
                continue;
            }

            pathElms.add(new TravelNode(node.getX(), node.getY(), node.getZ()));
        }

        return pathElms;
    }

}
