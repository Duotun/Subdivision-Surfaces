package Mesh;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jrtr.RenderContext;
import jrtr.VertexData;
import jrtr.glrenderer.*;
import jrtr.swrenderer.*;
import jrtr.*;

import javax.vecmath.Vector3f;


public class Edge {
    public Vertex a, b;
    public List<Triangle> faces;
    public Vertex ept;

    public Edge(Vertex a, Vertex b)
    {
        this.a = a;
        this.b = b;
        this.faces = new ArrayList<Triangle>();
    }

    public Edge()    //just for a default initilization
    {
        this.faces = new ArrayList<Triangle>();
    }

    public void AddTriangle(Triangle f)
    {
        faces.add(f);
    }

    public boolean Has(Vertex v)   //whether a vertex belongs to an edge
    {
        return v == a || v == b;
    }

    public Vertex GetOtherVertex(Vertex v)    //edge conecting to points
    {
        if (a != v) return a;
        return b;
    }
}