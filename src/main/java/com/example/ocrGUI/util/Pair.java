package com.example.ocrGUI.util;

public class Pair<E, V> {
        private E x;
        private V y;
        public Pair(E x, V y)
        {
            this.x = x;
            this.y = y;
        }
        public E getFirst(){
            return x;
        }
        public void setFirst(E x){ this.x = x; }
        public V getSecond(){
            return y;
        }
        public void setSecond(V y){ this.y = y; }

}
