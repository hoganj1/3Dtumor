package Misc;

import Tools.Genome;

@FunctionalInterface
public interface GenomeFn<T extends Genome>{
    void GenomeFn(T c);
}

