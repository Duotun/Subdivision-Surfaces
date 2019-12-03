package Mesh;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

import jrtr.RenderContext;
import jrtr.VertexData;
import jrtr.glrenderer.*;
import jrtr.swrenderer.*;
import jrtr.*;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import javax.vecmath.Vector3f;


public class Model {
    List<Vertex> vertices;
    List<Edge> edges;
    public List<Triangle> triangles;
    public Model()
    {
        this.vertices = new ArrayList<Vertex>();
        this.edges = new ArrayList<Edge>();
        this.triangles = new ArrayList<Triangle>();
    }
    public Model(Shape source)
    {

        this();
        float[] points = source.getVertexData().getElements().getLast().getData();   //getvertex data head so, make sure the first element is the vertex positions
        for (int i = 0, n = points.length, cnt=0; i < n; )
        {
            float x = points[i++]; float y = points[i++];
            float z = points[i++]; Vector3f tmpvec = new Vector3f(x,y,z);
            Vertex v = new Vertex(tmpvec, cnt++);   //cnt ->index
            vertices.add(v);
        }

        int[] triangles = source.getVertexData().getIndices();
        for (int i = 0, n = triangles.length; i < n; i += 3)
        {
            int i0 = triangles[i], i1 = triangles[i + 1], i2 = triangles[i + 2];
            Vertex v0 = vertices.get(i0), v1 = vertices.get(i1), v2 = vertices.get(i2);

            Edge e0 = GetEdge(edges, v0, v1);
            Edge e1 = GetEdge(edges, v1, v2);
            Edge e2 = GetEdge(edges, v2, v0);

            //System.out.println("Edge Point 1: "+e1.a.p);
            Triangle f = new Triangle(v0, v1, v2, e0, e1, e2);
            //System.out.println("Edge Point 1: "+f.e0.a.p);
            this.triangles.add(f);    //adjacent information
            v0.AddTriangle(f); v1.AddTriangle(f); v2.AddTriangle(f);
            e0.AddTriangle(f); e1.AddTriangle(f); e2.AddTriangle(f);
        }
    }
    Edge GetEdge(List<Edge> edges, Vertex v0, Vertex v1)
    {
        Edge match = null;
        for(int i =0;i<v0.edges.size();i++)
        {
            if(v0.edges.get(i).Has(v1))    //should obtain another
            {
                //System.out.println("v0: "+v0.p);
                //System.out.println("v1: "+v1.p);
                match =v0.edges.get(i);
                break;
            }
        }

        if (match!= null) return match;

        Edge ne = new Edge(v0, v1);
        //System.out.println("ne: "+v0.p);
        v0.AddEdge(ne);
        v1.AddEdge(ne);   //adjacent information
        this.edges.add(ne);
        return ne;
    }

    public void AddTriangle(Vertex v0, Vertex v1, Vertex v2)
    {
        if (!vertices.contains(v0)) vertices.add(v0);
        if (!vertices.contains(v1)) vertices.add(v1);
        if (!vertices.contains(v2)) vertices.add(v2);

        Edge e0 = GetEdge(v0, v1);   //counter-clockwise
        Edge e1 = GetEdge(v1, v2);
        Edge e2 = GetEdge(v2, v0);
        Triangle f = new Triangle(v0, v1, v2, e0, e1, e2);
        //System.out.println("Edge Point 1: "+e0.a.p);
        this.triangles.add(f);   //adjacent information
        v0.AddTriangle(f); v1.AddTriangle(f); v2.AddTriangle(f);
        e0.AddTriangle(f); e1.AddTriangle(f); e2.AddTriangle(f);
    }

    Edge GetEdge(Vertex v0, Vertex v1)
    {
        Edge match=null;
        for(int i =0;i<v0.edges.size();i++)
        {
            if(v0.edges.get(i).Has(v1))
            {

                match =v0.edges.get(i);
                break;
            }
        }

        if (match!= null) return match;

        Edge ne = new Edge(v0, v1);
        this.edges.add(ne);
        v0.AddEdge(ne);
        v1.AddEdge(ne);
        return ne;
    }

    //Build
    public VertexData Build(boolean weld, RenderContext r)
    {
        float normals[] = new float[this.vertices.size()*3];
        VertexData vertexData;
        int []triangless = new int[this.triangles.size()*3];   //indices
        if(weld)
        {
            vertexData= r.makeVertexData(this.vertices.size());
            for(int i=0,n=this.triangles.size();i<n;i++)
            {
                Triangle f = this.triangles.get(i);
                triangless[i*3] = vertices.indexOf(f.v0);
                triangless[i*3+1] = vertices.indexOf(f.v1);
                triangless[i*3+2] = vertices.indexOf(f.v2);
            }
            List<Vector3f> vertexpos = vertices.stream().map(v ->v.p).collect(Collectors.toList());
            float[] v = new float[this.vertices.size()*3];
            for(int i=0;i<vertexpos.size();)
            {
                v[3*i]=vertexpos.get(i).x;
                v[3*i+1]=vertexpos.get(i).y;
                v[3*i+2]=vertexpos.get(i).z; i++;
            }
            vertexData.addElement(v,VertexData.Semantic.POSITION,3);
        }
        else
        {
            vertexData= r.makeVertexData(this.triangles.size()*3);
            float[] v = new float[this.triangles.size()*3*3];
            for(int i =0, n =this.triangles.size();i<n;i++)
            {
                Triangle f = this.triangles.get(i);
                int i0 = i * 3, i1 = i * 3 + 1, i2 = i * 3 + 2;
                triangless[i0] = i0;
                triangless[i1] = i1;
                triangless[i2] = i2;

                v[3*i0]=f.v0.p.x;
                v[3*i0+1]=f.v0.p.y;
                v[3*i0+2]=f.v0.p.z;

                v[3*i1]=f.v1.p.x;
                v[3*i1+1]=f.v1.p.y;
                v[3*i1+2]=f.v1.p.z;

                v[3*i2]=f.v2.p.x;
                v[3*i2+1]=f.v2.p.y;
                v[3*i2+2]=f.v2.p.z;

            }
            vertexData.addElement(v,VertexData.Semantic.POSITION,3);
        }

        vertexData.addIndices(triangless);
        System.out.println("Triangles: "+triangless.length);
        normals = RecalculateNormals(vertexData);
        vertexData.addElement(normals,VertexData.Semantic.NORMAL,3);
        return vertexData;

    }

    //recalculate normals
    public static float[] RecalculateNormals(VertexData vertexData)
    {
        float []normals = new float[vertexData.getNumberOfVertices()*3];
        float v[] =vertexData.getElements().getLast().getData();
        int []triangless =vertexData.getIndices();
        Vector3f []vecnormals = new Vector3f[vertexData.getNumberOfVertices()];
        for(int i=0;i<vertexData.getNumberOfVertices();i++) {
            vecnormals[i] =new Vector3f(0f,0f,0f);
        }
        Vector3f v1 =new Vector3f();Vector3f v2 =new Vector3f(); Vector3f v3 =new Vector3f();
        Vector3f normaltmp = new Vector3f(0,0,0);
        //System.out.println(triangless.length);
        for(int i=0, n=triangless.length;i<n;)
        {
            int i0=triangless[i++];
            int i1=triangless[i++];
            int i2=triangless[i++];  //vertex index

            v1.x = v[3*i0]; v1.y=v[3*i0+1];v1.z=v[3*i0+2];
            v2.x = v[3*i1]; v2.y=v[3*i1+1];v2.z=v[3*i1+2];
            v3.x = v[3*i2]; v3.y=v[3*i2+1];v3.z=v[3*i2+2];
            //System.out.println("v1: "+v1.x);

            normaltmp=CalculateSurfaceNormal(v1,v2,v3);

            vecnormals[i0].add(normaltmp);
            vecnormals[i1].add(normaltmp);
            vecnormals[i2].add(normaltmp);
        }
        for(int i=0;i<vecnormals.length;i++)
        {
            vecnormals[i].normalize();   //average vertex normal
            //System.out.println("Normal: "+vecnormals[i]);
        }
        for(int i=0;i<vecnormals.length;i++)
        {
            normals[3*i] =vecnormals[i].x;
            normals[3*i+1] =vecnormals[i].y;
            normals[3*i+2] =vecnormals[i].z;

        }
        return normals;
    }
    public static Vector3f CalculateSurfaceNormal(Vector3f v1, Vector3f v2, Vector3f v3){
        Vector3f normal=new Vector3f(0,0,0);
        v2.sub(v1); v3.sub(v1);   //v2-v1, v3-v1
        normal.cross(v2,v3);  //cross product
        normal.normalize();   //normalized normal
        //System.out.println("Normal: "+normal);
        return normal;
    }

    //continue label to outer loop
    //delete repeating vertex
    public static VertexData Weld(VertexData vertexData, float threshold,RenderContext r)
    {
        float [] oldVertices = vertexData.getElements().getLast().getData();
        float[] newVertices = new float[vertexData.getNumberOfVertices()*3];
        int []old2new = new int[vertexData.getNumberOfVertices()];
        int newSize=0;
        Vector3f min=new Vector3f(Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE);
        Vector3f max=new Vector3f(Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE);

        //Find bounding volume (AABB)
        for(int i=0;i<vertexData.getNumberOfVertices();i++)
        {
            if(oldVertices[3*i]<min.x) min.x = oldVertices[3*i];
            if(oldVertices[3*i+1]<min.y) min.y = oldVertices[3*i+1];
            if(oldVertices[3*i+2]<min.z) min.z = oldVertices[3*i+2];
            if(oldVertices[3*i]>max.x) max.x = oldVertices[3*i];
            if(oldVertices[3*i+1]>max.y) max.y = oldVertices[3*i+1];
            if(oldVertices[3*i+2]>max.z) max.z = oldVertices[3*i+2];
        }
        int bucketSizeX = 2;
        int bucketSizeY = 2;
        int bucketSizeZ = 2;
        ArrayList<Integer>[][][] buckets = new ArrayList [bucketSizeX][bucketSizeY][bucketSizeZ];

        skip:
        for(int i=0;i<vertexData.getNumberOfVertices();i++)
        {
            int x = Math.round((oldVertices[3*i]-min.x)/(max.x-min.x));
            int y = Math.round((oldVertices[3*i+1]-min.y)/(max.y-min.y));
            int z = Math.round((oldVertices[3*i+2]-min.z)/(max.z-min.z));

            if(buckets[x][y][z]==null) buckets[x][y][z]=new ArrayList<Integer>();

            for(int j=0;j<buckets[x][y][z].size();j++) {

                Vector3f to =new Vector3f();
                to.x = newVertices[3*buckets[x][y][z].get(j)]-oldVertices[3*i];
                to.y = newVertices[3*buckets[x][y][z].get(j)+1]-oldVertices[3*i+1];
                to.z = newVertices[3*buckets[x][y][z].get(j)+2]-oldVertices[3*i+2];
                if(to.lengthSquared()<threshold) {

                    old2new[i] = buckets[x][y][z].get(j);
                    continue skip;
                }
            }

            newVertices[3*newSize]=oldVertices[3*i];
            newVertices[3*newSize+1]=oldVertices[3*i+1];
            newVertices[3*newSize+2]=oldVertices[3*i+2];
            buckets[x][y][z].add(newSize);  //redefine index based on newSize
            old2new[i]=newSize;
            newSize++;
        }
        int[]oldTris =vertexData.getIndices();
        int[]newTris = new int[oldTris.length];
        for(int i=0;i<oldTris.length;i++)
        {
            newTris[i] = old2new[oldTris[i]];
        }
        float [] v = new float[newSize*3];
        for(int i=0;i<newSize;i++)
        {
            v[3*i] = newVertices[3*i];
            v[3*i+1] = newVertices[3*i+1];
            v[3*i+2] = newVertices[3*i+2];

        }
        VertexData vertexDatanew =r.makeVertexData(newSize);
        vertexDatanew.addElement(v,VertexData.Semantic.POSITION,3);
        vertexDatanew.addIndices(newTris);
        float []normals = new float[newSize*3];
        normals = RecalculateNormals(vertexDatanew);
        //System.out.println("v: "+v[i]);
        vertexDatanew.addElement(normals,VertexData.Semantic.NORMAL,3);

        return vertexDatanew;
    }
}