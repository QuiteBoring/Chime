package chime.processor;

import chime.path.PathElm;

import java.util.List;

public abstract class Processor {

    public abstract void process(List<PathElm> elms);

}
