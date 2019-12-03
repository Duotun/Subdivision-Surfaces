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
import sun.nio.cs.ext.MacThai;

import javax.vecmath.Vector3f;

public class Vertex {
    public Vector3f p;
    public List<Edge> edges;
    public List<Triangle> triangles;
    public Vertex updated;
    public int index;

    public Vertex()
    {
        this.p = new Vector3f(Float.NaN,Float.NaN,Float.NaN);
        this.index = -1;
        this.edges = new ArrayList<Edge>();
        this.triangles = new ArrayList<Triangle>();

    }
    public Vertex(Vector3f p)
    {
        this.p = p;
        this.index = -1;
        this.edges = new ArrayList<Edge>();
        this.triangles = new ArrayList<Triangle>();
    }

    public Vertex(Vector3f p, int index)
    {
        this.p = p;
        this.index = index;
        this.edges = new ArrayList<Edge>();
        this.triangles = new ArrayList<Triangle>();
    }

    public void AddEdge(Edge e)
    {
        edges.add(e);   //add edge at the end of the list
    }

    public void AddTriangle(Triangle f)
    {
        triangles.add(f);
    }
}