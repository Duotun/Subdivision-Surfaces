package Mesh;

import java.util.*;
import java.util.stream.Collectors;

import jrtr.RenderContext;
import jrtr.VertexData;
import jrtr.glrenderer.*;
import jrtr.swrenderer.*;
import jrtr.*;
import javax.vecmath.Vector3f;
public class SubdivisionSurface {

    public static Shape Subdivide(Shape source, int details, boolean weld, RenderContext r)
    {
        Model model = Subdivide(source,details);
        Shape mesh = new Shape(model.Build(weld,r));
        return mesh;
    }
    public static Model Subdivide(Shape source, int details)
    {
        Model model = new Model(source);
        //System.out.println(("Edge Point now: "+model.triangles.get(0).e0.a.p));
        SubdivisionSurface divider = new SubdivisionSurface();
        for(int i=0;i<details;i++)  //recursive division
        {
            model=divider.Divide(model);
        }
        return model;
    }

    Edge GetEdge(List<Edge> edges, Vertex v0, Vertex v1)
    {
        Edge match=new Edge();
        for(int i =0;i<v0.edges.size();i++)
        {
            if(v0.edges.get(i).Has(v1))
            {
                match =v0.edges.get(i);
                break;
            }
        }

        if (match.Has(v0) != false) return match;

        Edge ne = new Edge(v0, v1);
        v0.AddEdge(ne);
        v1.AddEdge(ne);   //adjacent information
        edges.add(ne);
        return ne;
    }
    Model Divide(Model model)   //according to triangles to divide
    {
        Model nmodel =new Model();
        //System.out.println(model.vertices.get(0).p);
        //System.out.println(("Edge Point now: "+model.triangles.get(0).e0.a.p));
        for(int i=0, n=model.triangles.size();i<n;i++)
        {
            Triangle f = model.triangles.get(i);
            //System.out.println("e0: before "+f.e0.a.p);
            Vertex ne0 = GetEdgePoint(f.e0);
            Vertex ne1 = GetEdgePoint(f.e1);
            Vertex ne2 = GetEdgePoint(f.e2);

            //System.out.println("e0: after "+f.e0.a.p);
            //System.out.println("e1: "+ne1.p);
            Vertex nv0 = GetVertexPoint(f.v0);
            Vertex nv1 = GetVertexPoint(f.v1);
            Vertex nv2 = GetVertexPoint(f.v2);

            nmodel.AddTriangle(nv0,ne0,ne2);
            nmodel.AddTriangle(ne0,nv1,ne1);
            nmodel.AddTriangle(ne0,ne1,ne2);
            nmodel.AddTriangle(ne2,ne1,nv2);
        }

        return nmodel;
    }

    //compute odd vertex, loop division
    public Vertex GetEdgePoint(Edge e)
    {
        if(e.ept!=null) return e.ept;
        if(e.faces.size()!=2)   //boundary odd vertices
        {
            Vector3f m =new Vector3f(e.a.p);
            m.add(e.b.p); m.scale(0.5f);
            e.ept = new Vertex(m,e.a.index);
        }else
        {
            final float alpha =3f/8f;
            final float beta = 1f/8f;
            Vertex up = e.faces.get(0).GetOtherVertex(e);
            //System.out.println("Edge Point: "+up.p);
            Vertex down = e.faces.get(1).GetOtherVertex(e);

            Vector3f m = new Vector3f(e.a.p); m.add(e.b.p); m.scale(alpha);
            Vector3f n = new Vector3f(up.p); n.add(down.p); n.scale(beta);
            m.add(n);
            //System.out.println("Edge Point: "+m);
            e.ept = new Vertex(m,e.a.index);

        }

        return e.ept;
    }

    public Vertex[] GetAdjacies(Vertex v)  //get neighboring vertices
    {
        Vertex [] adjaciencies = new Vertex[v.edges.size()];
        for(int i=0, n=v.edges.size();i<n;i++)
        {
            adjaciencies[i] =v.edges.get(i).GetOtherVertex(v);
        }
        return adjaciencies;

    }


    //compute even vertices
    public Vertex GetVertexPoint(Vertex v)
    {
        if(v.updated !=null) return v.updated;

        Vertex [] adjacies =GetAdjacies(v);
        int n = adjacies.length;
        if(n<3)   //boundary
        {
            Vertex e0 = v.edges.get(0).GetOtherVertex(v);
            Vertex e1 = v.edges.get(1).GetOtherVertex(v);
            final float k0 = 3f/4f;
            final float k1= 1f/8f;
            Vector3f mv = new Vector3f(e0.p);  mv.add(e1.p); mv.scale(k1);
            Vector3f nv = new Vector3f(v.p); nv.scale(k0); mv.add(nv);
            v.updated = new Vertex(nv, v.index);

        }
        else
        {
            final double pi2 = Math.PI*2.0;
            final float k0 = 5f/8f;
            final float k1 = 3f/8f;
            final float k2 = 1f/4f;

            float alpha =(n==3)?3f/16f: (1f/n)*(5f/8f-(float)Math.pow(3.0/8.0+1.0/4.0*Math.cos(pi2/n),2.0));
            //System.out.println("Alpha: "+alpha);
            Vector3f np = new Vector3f(v.p); np.scale(1f-(float)n*alpha);
            for(int i=0;i<n;i++)
            {
                Vertex adj = adjacies[i];
                Vector3f tmp =new Vector3f(adj.p);
                tmp.scale(alpha); np.add(tmp);
            }

            v.updated = new Vertex(np,v.index);
            //System.out.println("New Point" + v.updated.p);
        }
        return v.updated;
    }

}