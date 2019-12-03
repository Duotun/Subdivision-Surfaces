package Mesh;

import java.awt.Color;
import java.awt.Point;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import jrtr.RenderContext;
import jrtr.VertexData;
import jrtr.glrenderer.*;
import jrtr.swrenderer.*;
import jrtr.*;
public class BasicShapes {


    //periodic cos and sin
    private static final float cos(int k, int res) {

        return ((Double)Math.cos(2*Math.PI/(double)res*k)).floatValue();
    }

    private static final float sin(int k, int res) {
        return ((Double)Math.sin(2*Math.PI/(double)res*k)).floatValue();
    }


    //diamond-square steps -> height values
    public static Shape makeFractalLandscape(int n, float noise, RenderContext r)
    {
        assert n>0;
        // 2^n+1, one dimension
        int size =(int)(Math.pow(2,n)+1);
        float [][]heightvalues= new float[size][size];
        computeHeightValues(heightvalues,size,noise);

        float maxHight = 0.1f;  //the highest one
        int numberofVertices =size*size;
        float v[]=new float[3*numberofVertices];

        for(int x=0; x<size; x++)
            for(int y=0; y<size; y++)
            {
                maxHight = Float.max(maxHight, heightvalues[x][y]);
            }
        //System.out.println(maxHight);
        for(int i=0;i<size;i++)
        {
           for(int j=0;j<size;j++)
           {
                v[3*(i*size+j)]=i-(int)size/2;
                v[3*(i*size+j)+1]= heightvalues[i][j];
                v[3*(i*size+j)+2]=j-(int)size/2;

           }
        }
        // two triangles per sqaure, three vertex indices for each triangle
        int indices[] = new int[2*3*(int)Math.pow(4,n)]; //be care the number of square = size -1
        for(int i=0;i<size-1;i++)
        {
            for(int j =0;j<size-1;j++)
            {
                //bottom triangle
                indices[6*(i*(size-1)+j)]=i*size+j;
                indices[6*(i*(size-1)+j)+1]=(i+1)*size+j;
                indices[6*(i*(size-1)+j)+2]=(i+1)*size+j+1;

                //upper triangle
                indices[6*(i*(size-1)+j)+3]=(i+1)*size+j+1;
                indices[6*(i*(size-1)+j)+4]=(i)*size+j+1;
                indices[6*(i*(size-1)+j)+5]=(i)*size+j;
            }
        }

        float c[]=new float[3*numberofVertices];
        for(int i=0;i<size;i++)
        {
            for(int j =0;j<size;j++)
            {
                Color color = getlandscapeColor(heightvalues[i][j],maxHight);
                float red = color.getRed();
                float green= color.getGreen();
                float blue = color.getBlue();

                c[3*(i*size+j)] = red/255.0f;
                c[3*(i*size+j)+1]= green/255.0f;
                c[3*(i*size+j)+2]= blue/255.0f;

                //c[3*(i*size+j)] = 1;
                //c[3*(i*size+j)+1]= 1;
                //c[3*(i*size+j)+2]= 1;

            }
        }
        float[] normals = new float[3*numberofVertices];
        for(int x=0; x<size; x++)
            for(int y=0; y<size; y++)
            {
                int indexOfVertex = 3*(x*size + y);
                int indexOfNextRowVertex = x==(size-1) ? 3*((x-1)*size + y) : 3*((x+1)*size + y);
                int indexOfNextColumnVertex = y==(size-1) ? 3*(x*size + y-1) : 3*(x*size + y+1);

                Vector3f vertex = new Vector3f(v[indexOfVertex],v[indexOfVertex+1],v[indexOfVertex+2]);
                Vector3f rowVertex = new Vector3f(v[indexOfNextRowVertex],v[indexOfNextRowVertex+1],v[indexOfNextRowVertex+2]);
                Vector3f columnVertex = new Vector3f(v[indexOfNextColumnVertex],v[indexOfNextColumnVertex+1],v[indexOfNextColumnVertex+2]);
                Vector3f crossP = new Vector3f();
                rowVertex.sub(vertex);
                columnVertex.sub(vertex);
                if(x == size-1 || y == size-1)
                    crossP.cross(columnVertex,rowVertex);
                else
                    crossP.cross(rowVertex,columnVertex);

                normals[3*(x*size + y)] = crossP.x;
                normals[3*(x*size + y)+1] = crossP.y;
                normals[3*(x*size + y)+2] = crossP.z;
            }

        VertexData vertexData = r.makeVertexData(numberofVertices);
        vertexData.addElement(v,VertexData.Semantic.POSITION,3);
        vertexData.addElement(c,VertexData.Semantic.COLOR,3);
        //vertexData.addElement(normals,VertexData.Semantic.NORMAL,3);
        vertexData.addIndices(indices);
        Shape fractalLandscape = new Shape(vertexData);
        return fractalLandscape;
    }

    private static Color getlandscapeColor(float height, float maxHight)
    {
        assert maxHight >0f;
        Color sand = new Color(237, 201, 175);
        Color leaf = new Color(30, 147, 45);
        Color mountain = new Color(150, 141, 153);
        Color snow = new Color(255,255,255);

        float relativeHeight = height / maxHight; // between 0 and 1

        if(relativeHeight < 0.65f)
            return sand;
        else if(relativeHeight < 0.75f)
            return leaf;
        else if(relativeHeight < 0.9f)
            return mountain;
        else
            return snow;
    }
    //final simply makes the object reference unchangeable. (still change value for the pointers)
    // The object it points to is not immutable by doing this. I
    // INSTANCE can never refer to another object, but the object it refers to may change state.
    private static void computeHeightValues(final float[][]heightvalues,int size,float noise)
    {
        //size is always an odd number.
        int arrayposHalfsize=size/2;
        int currentSize=size;
        float currentNoise= noise;

        ArrayList<Point> iterationPos =new ArrayList<Point>();
        ArrayList<Point> newPos = new ArrayList<Point>();
        iterationPos.add(new Point(0,0));


        //random initialization
        heightvalues[0][0]=(float)(Math.random()+0.1)*currentNoise+0.1f;
        heightvalues[size-1][0]=(float)(Math.random()+0.1)*currentNoise+0.1f;
        heightvalues[0][size-1]=(float)(Math.random()+0.1)*currentNoise+0.1f;
        heightvalues[size-1][size-1]=(float)(Math.random()+0.1)*currentNoise+0.1f;

        //iteration only cares about the beginning points (add three for each diamond step)
        while(currentSize>2)
        {
            //square steps
            for(Point point:iterationPos)
            {
                applySquareStep(heightvalues,point.x,point.y,arrayposHalfsize,currentSize,currentNoise);
            }
            //diamond steps, add 3 new points each time for next iteration
            for(Point point:iterationPos)
            {
                applyDiamondStep(heightvalues,point.x,point.y,arrayposHalfsize,currentSize,currentNoise);
                newPos.add(new Point(point.x+arrayposHalfsize,point.y));
                newPos.add(new Point(point.x+arrayposHalfsize,point.y+arrayposHalfsize));
                newPos.add(new Point(point.x,point.y+arrayposHalfsize));
            }
            //next iteration preparation
             currentSize =arrayposHalfsize+1;
             arrayposHalfsize=currentSize/2;
             iterationPos.addAll(newPos);
             newPos.clear();
             currentNoise*=0.8f;

        }

    }

    private static void applySquareStep(final float[][]heightvalues, int originx,int originy, int halfsize,int size, float noise)
    {
       heightvalues[originx+halfsize][originy+halfsize]= calculateMeans(new float[]{
           heightvalues[originx][originy], heightvalues[originx+size-1][originy],
           heightvalues[originx][originy+size-1],heightvalues[originx+size-1][originy+size-1]
       },noise);



    }

    private static void applyDiamondStep(final float[][]heightvalues, int originx,int originy, int halfsize,int size, float noise)
    {
        // four points, utilize NaN to process whether it is a boundary here
        heightvalues[originx+halfsize][originy] = calculateMeans(new float[]
                {
                        heightvalues[originx][originy], heightvalues[originx+size-1][originy],
                        heightvalues[originx+halfsize][originy+halfsize],(originy-halfsize)<0?Float.NaN:heightvalues[originx+halfsize][originy-halfsize]
                },noise);

        heightvalues[originx][originy+halfsize]=calculateMeans(new float[]{
                heightvalues[originx][originy],heightvalues[originx+halfsize][originy+halfsize],
                heightvalues[originx][originy+size-1],(originx-halfsize)<0?Float.NaN:heightvalues[originx-halfsize][originy+halfsize]
        },noise);

        heightvalues[originx+size-1][originy+halfsize]=calculateMeans(new float[]{
                heightvalues[originx+halfsize][originy+halfsize],heightvalues[originx+size-1][originy],
                heightvalues[originx+size-1][originy+size-1],(originx+size-1+halfsize)>=heightvalues.length?Float.NaN:heightvalues[originx+size-1+halfsize][originy+halfsize]
        },noise);

        heightvalues[originx+halfsize][originy+size-1]=calculateMeans(new float[]{
            heightvalues[originx+halfsize][originy+halfsize],heightvalues[originx][originy+size-1],
                heightvalues[originx+size-1][originy+size-1],(originy+size-1+halfsize)>=heightvalues.length?Float.NaN:heightvalues[originx+halfsize][originy+size-1+halfsize]
        },noise);


    }
    //generally three of four heights
    private static float calculateMeans(float[]heights, float noise)
    {
        float mean=0f;
        float numberofFloats=0f;

        for(int i=0;i< heights.length;i++)
        {
            mean+=Float.isNaN(heights[i])?0:heights[i];
            numberofFloats+=Float.isNaN(heights[i])?0:1f;
        }
        //Random rnd = new Random(System.currentTimeMillis());
        return mean/numberofFloats+(float)(Math.random()+0.1f)*noise/3f;
    }

    public static final VertexData makeCylinderwithoutnormals(int resolution, float height, float radius, RenderContext renderContext)
    {
        assert resolution >=3;

        int number_vertices = 8+2*(resolution-3);  //at least 3 segments, and adds 2 vertices to new segments
        int index_Topcenter = number_vertices/2;   //first down disk, and then top disk

        //the vertex positions
        float []v = new float[3*number_vertices];  //3d
        float []uv = new float[2*number_vertices];  //3d

        //bottom center vertex
        v[0] = 0;
        v[1] = -height/2.0f;  //y
        v[2] = 0;

        //up center vertex
        v[3*index_Topcenter] = 0;
        v[3*index_Topcenter + 1] = height*0.5f;
        v[3*index_Topcenter + 2] = 0;

        int angle_index = 0;
        for(int i = 3; i< 3*index_Topcenter;i+=3)
        {
            float x = radius * BasicShapes.cos(angle_index,resolution);
            float z = radius * BasicShapes.sin(angle_index, resolution);

            v[i] = x;
            v[i+1] = -height/2.0f;
            v[i+2] = z;

            v[3*index_Topcenter+i] = x;
            v[3*index_Topcenter+i+1] = height/2.0f;
            v[3*index_Topcenter+i+2] = z;
            angle_index++;
        }

        int modu=0;
        for(int i=2;i<2*index_Topcenter;i+=4)
        {
             uv[i]=modu;
             uv[i+1]=0;

             uv[2*index_Topcenter+i]=modu;
             uv[2*index_Topcenter+i+1]=1;
             modu=(modu+1)%2;
        }

        uv[0]=1;
        uv[1]=1;

        uv[2*index_Topcenter]=1;
        uv[2*index_Topcenter+1]=1;


        //The vertex color
        float c[] = new float[3*number_vertices];

        for(int i=3;i<3*resolution; i+=6)   //fading black and white
        {
            c[i] = 1;
            c[i+1] = 1;
            c[i+2] = 1;

            c[3*index_Topcenter+i] = 1;
            c[3*index_Topcenter+i+1] = 1;
            c[3*index_Topcenter+i+2] = 1;

        }

        // set color of bottom center-vertex
        c[0] = 1;
        c[1] = 1;
        c[2] = 1;
        // set color of top center-vertex
        c[3*index_Topcenter] = 1;
        c[3*index_Topcenter+1] = 1;
        c[3*index_Topcenter+2] = 1;

        //index
        int indices [] = new int[3*4*resolution];   // 4 triangles per segment
        for(int i=0; i<resolution;i++)
        {
            //bottom disk
            indices[12*i] = 0;
            indices[12*i+1] = i+1;
            indices[12*i+2] = i+2 ==resolution+1? 1: i+2;

            //top disk
            indices[12*i+3] = index_Topcenter;
            indices[12*i+4] = index_Topcenter+i+1;
            indices[12 * i + 5] = ((index_Topcenter + i + 2) == number_vertices) ? index_Topcenter + 1 : index_Topcenter + i+2;

            //build side segment (two triangles)
            //bottom one
            indices[12*i+6] = i+1;
            indices[12*i+7] = i+2 ==resolution+1? 1: i+2;
            indices[12*i+8] = index_Topcenter+i+1;

            //top one
            indices[12*i+9] = i+2 ==resolution+1? 1: i+2;;
            indices[12*i+10] = index_Topcenter+i+2 ==number_vertices? index_Topcenter + 1 : index_Topcenter + i+2;
            indices[12*i+11] = index_Topcenter+i+1;

        }

       //adding UV coordinates now!


        VertexData vertexData = renderContext.makeVertexData(number_vertices);
        vertexData.addElement(v,VertexData.Semantic.POSITION,3);
        vertexData.addElement(c,VertexData.Semantic.COLOR,3);
        vertexData.addElement(uv,VertexData.Semantic.TEXCOORD,2);
        vertexData.addIndices(indices);

        return vertexData;
    }

    //torus
    public static final VertexData makeToruswithoutnormals(int innerResolution, int outerResolution, float innerRadius, float outerRadius, RenderContext renderContext)
    {
        assert innerResolution >=3;
        assert outerResolution >=3;
        int number_vertices = innerResolution * outerResolution;

        double angleInnerUnit =  (2.0*Math.PI)/innerResolution;
        double angleOuterUnit =  (2.0*Math.PI)/outerResolution;
        double currentInnerAngle = 0.0;
        double currentOuterAngle = 0.0;
        float v[] = new float[number_vertices*3];   //3d

        for(int i=0;i<innerResolution; i++)
        {
            //innerradius - R, outerradius - r
            for(int j=0;j<3*outerResolution; j+=3)
            {
                float x = (float)((innerRadius+outerRadius*Math.cos(currentOuterAngle))*Math.cos(currentInnerAngle));
                float y = (float)(outerRadius * Math.sin(currentOuterAngle));
                float z = (float)((innerRadius+outerRadius*Math.cos(currentOuterAngle))*Math.sin(currentInnerAngle));

                v[3*i*outerResolution+j] = x;
                v[3*i*outerResolution+j+1] = y;
                v[3*i*outerResolution+j+2] = z;

                currentOuterAngle+=angleOuterUnit;
            }
            currentOuterAngle = 0;   // start another outer circle.
            currentInnerAngle += angleInnerUnit;

        }

        float c[] = new float[3*number_vertices];
        for(int i=0;i<innerResolution;i++)
        {
            float r,g,b;
            //r = 1f; g = 1f;b = 1f;
            r = (float)Math.random();
            g = (float)Math.random();
            b = (float)Math.random();
            for(int j=0;j<3*outerResolution;j+=3)
            {
                c[i*outerResolution*3+j] = r;
                c[i*outerResolution*3+j+1] = g;
                c[i*outerResolution*3+j+2] = b;

            }

        }

        int indices[] = new int[3*2*number_vertices];  //two triangles per face

        for(int i= 0; i<innerResolution;i++)
        {
            for(int j=0;j < outerResolution;j++)
            {
                //two triangles - counterclockwise!
                indices[i*6*outerResolution+6*j] = (i*outerResolution+j) % number_vertices;
                indices[i*6*outerResolution+6*j+1] = (i*outerResolution+j+1)%number_vertices;
                indices[i*6*outerResolution+6*j+2] = ((i+1)*outerResolution+j)%number_vertices;

                indices[i*6*outerResolution+6*j+3] = (i*outerResolution+j+1) % number_vertices;
                indices[i*6*outerResolution+6*j+4] = ((i+1)*outerResolution+j)%number_vertices;
                indices[i*6*outerResolution+6*j+5] = ((i+1)*outerResolution+j+1)%number_vertices;

            }

        }

        VertexData vertexData = renderContext.makeVertexData(number_vertices);
        vertexData.addElement(v,VertexData.Semantic.POSITION,3);
        vertexData.addElement(c,VertexData.Semantic.COLOR,3);
        vertexData.addIndices(indices);

        return vertexData;
    }

    public static final Shape makeCylinder(int resolution, float height, float radius, RenderContext renderContext)
    {
        assert resolution >= 3;

        float halfHeight = height/2;


        float[] vertices = new float[12*resolution + 6 + 6]; 	// 3 per vertex, n + 1 per disk, 2 disks
        float[] normals = new float[12*resolution + 6 + 6];

        // set vertices and normals
        // center points
        // top center
        vertices[12*resolution] = 0;
        vertices[12*resolution + 1] = halfHeight;
        vertices[12*resolution + 2] = 0;

        normals[12*resolution + 1] = 1;

        //bottom center
        vertices[12*resolution + 3] = 0;
        vertices[12*resolution + 4] = -halfHeight;
        vertices[12*resolution + 5] = 0;

        normals[12*resolution + 4] = -1;

        // disk vertices
        for (int k = 0; k < resolution; k++) {
            // top disk
            vertices[3*k] = radius * cos(k, resolution);
            vertices[3*k+1] = halfHeight;
            vertices[3*k+2] = radius * sin(k, resolution);

            normals[3*k + 1] = 1;   //up

            // bottom disk
            vertices[3*resolution + 3*k] = radius * cos(k, resolution);
            vertices[3*resolution + 3*k + 1] = -halfHeight;
            vertices[3*resolution + 3*k + 2] = radius * sin(k, resolution);

            normals[3*resolution + 3*k + 1] = -1;   //down
        }

        // side vertices
        for (int k = 0; k < resolution; k++) {
            // top disk
            vertices[6*resolution+3*k] = radius * cos(k, resolution);
            vertices[6*resolution+3*k+1] = halfHeight;
            vertices[6*resolution+3*k + 2] = radius * sin(k, resolution);

            normals[6*resolution+3*k-1] = cos(k, resolution);    //present vertex directions are enough
            normals[6*resolution+3*k+1] = sin(k, resolution);

            // bottom disk
            vertices[9*resolution + 3*k] = radius * cos(k, resolution);
            vertices[9*resolution + 3*k + 1] = -halfHeight;
            vertices[9*resolution + 3*k + 2] = radius * sin(k, resolution);

            normals[9*resolution + 3*k-1] = cos(k, resolution);
            normals[9*resolution + 3*k+1] = sin(k, resolution);
        }

        // texturing vertices
        vertices[12*resolution + 6] = radius;
        vertices[12*resolution + 7] = halfHeight;
        vertices[12*resolution + 8] = 0;
        normals[12*resolution + 6] = 1;

        vertices[12*resolution + 9] = radius;
        vertices[12*resolution + 10] = -halfHeight;
        vertices[12*resolution + 11] = 0;
        normals[12*resolution + 9] = 1;

        // set colors
        float[] colors = new float[12*resolution + 6 + 6];
        for (int i = 0; i < 6*resolution+1; i+=6) {
            colors[i] = 1;
            colors[i+1] = 1;
            colors[i+2] = 1;
            colors[6*resolution + i] = 1;
            colors[6*resolution + i + 1] = 1;
            colors[6*resolution + i + 2] = 1;
        }

        // Texture coordinates - use texture with ratio 10:1
        float uv[] = new float[2*vertices.length/3]; // 2 per vertex (#vertices = vertices.length/3)

        // top disk center
        uv[2*4*resolution] = 0.95f;
        uv[2*4*resolution+1] = 0.5f;

        // bottom disk center
        uv[2*(4*resolution + 1)] = 0.95f;
        uv[2*(4*resolution + 1)+1] = 0.5f;

        for(int k = 0; k < resolution; k++) {
            // top disk circle
            uv[2*k] = 0.95f + cos(k, resolution)*0.05f;
            uv[2*k+1] = 0.5f + sin(k, resolution)*0.5f;

            // bottom disk circle
            uv[2*(resolution + k)] = 0.95f + cos(k, resolution)*0.05f;
            uv[2*(resolution + k)+1] = 0.5f + sin(k, resolution)*0.5f;

            // top side vertices
            uv[2*(2*resolution + k)] = 0;
            uv[2*(2*resolution + k)+1] = (float)k/(resolution-1);

            // bottom side vertices
            uv[2*(3*resolution + k)] = 0.9f;
            uv[2*(3*resolution + k)+1] = (float)k/(resolution-1);
        }
        // top disk texturing vertex
        uv[2*(4*resolution+2)] = 0;
        uv[2*(4*resolution+2)+1] = 0;

        // bottom disk texturing vertex
        uv[2*(4*resolution + 3)] = 0.9f;
        uv[2*(4*resolution + 3)+1] = 0;

        // set indices
        int[] indices = new int[12*resolution]; // 4n triangles, 3 per triangle

        for(int k = 0; k < resolution; k++) {
            // top disk
            indices[12*k] = 4*resolution;
            indices[12*k+1] = k;
            indices[12*k+2] = k + 1;

            // bottom disk
            indices[12*k+3] = 4*resolution + 1;
            indices[12*k+4] = resolution + k + 1;
            indices[12*k+5] = resolution + k;

            // side
            indices[12*k+6] = 2 * resolution + k + 1;
            indices[12*k+7] = 2 * resolution + k;
            indices[12*k+8] = 3 * resolution + k;

            indices[12*k+9] = 3 * resolution + k;
            indices[12*k+10] = 3 * resolution + k + 1;
            indices[12*k+11] = 2 * resolution + k + 1;
        }

        // finishing
        indices[12*(resolution-1)] = 4*resolution;
        indices[12*(resolution-1)+1] = resolution - 1;
        indices[12*(resolution-1)+2] = 0;

        indices[12*(resolution-1)+3] = 4*resolution + 1;
        indices[12*(resolution-1)+4] = resolution;
        indices[12*(resolution-1)+5] = 2*resolution - 1;

        indices[12*(resolution-1)+6] = 4*resolution+2; // 2*resolution; // top right
        indices[12*(resolution-1)+7] = 3*resolution - 1; // top left
        indices[12*(resolution-1)+8] = 4*resolution - 1; // bottom left

        indices[12*(resolution-1)+9] = 4*resolution - 1; // bottom left
        indices[12*(resolution-1)+10] = 4*resolution+3; // 3*resolution; // bottom right
        indices[12*(resolution-1)+11] = 4*resolution+2; // 2*resolution; // top right

        VertexData vertexData = renderContext.makeVertexData(4*resolution + 2 + 2);
        vertexData.addElement(vertices, VertexData.Semantic.POSITION, 3);
        vertexData.addElement(colors, VertexData.Semantic.COLOR, 3);
        vertexData.addElement(normals, VertexData.Semantic.NORMAL, 3);
        vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);
        vertexData.addIndices(indices);

        Shape cylinder = new Shape(vertexData);
        return cylinder;
    }

    public static VertexData makeCube(RenderContext renderContext)
    {
        // Make a simple geometric object: a cube
        // a face is defined by two vertices
        // The vertex positions of the cube
        float v[] = {-1,-1,1, 1,-1,1, 1,1,1, -1,1,1,		// front face
                -1,-1,-1, -1,-1,1, -1,1,1, -1,1,-1,	// left face
                1,-1,-1,-1,-1,-1, -1,1,-1, 1,1,-1,		// back face
                1,-1,1, 1,-1,-1, 1,1,-1, 1,1,1,		// right face
                1,1,1, 1,1,-1, -1,1,-1, -1,1,1,		// top face
                -1,-1,1, -1,-1,-1, 1,-1,-1, 1,-1,1};	// bottom face

        // The vertex normals
        float n[] = {0,0,1, 0,0,1, 0,0,1, 0,0,1,			// front face
                -1,0,0, -1,0,0, -1,0,0, -1,0,0,		// left face
                0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1,		// back face
                1,0,0, 1,0,0, 1,0,0, 1,0,0,			// right face
                0,1,0, 0,1,0, 0,1,0, 0,1,0,			// top face
                0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0};		// bottom face

        // The vertex colors
        float c[] = {0,0,1, 0,0,1, 0,0,1, 0,0,1,
                1,0,0, 1,0,0, 1,0,0, 1,0,0,
                0,1,0, 0,1,0, 0,1,0, 0,1,0,
                1,0,1, 1,0,1, 1,0,1, 1,0,1,
                0,1,1, 0,1,1, 0,1,1, 0,1,1,
                0.8f,0.8f,0.8f, 0.8f,0.8f,0.8f, 0.8f,0.8f,0.8f, 0.8f,0.8f,0.8f};   //pure white here

        // Texture coordinates
        float uv[] = {0,0, 1,0, 1,1, 0,1,
                0,0, 1,0, 1,1, 0,1,
                0,0, 1,0, 1,1, 0,1,
                0,0, 1,0, 1,1, 0,1,
                0,0, 1,0, 1,1, 0,1,
                0,0, 1,0, 1,1, 0,1};

        // Construct a data structure that stores the vertices, their
        // attributes, and the triangle mesh connectivity
        //numbers of vertices
        VertexData vertexData = renderContext.makeVertexData(24);
        //the last parameter should be the dimension, n components
        vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
        vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
        vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
        vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);

        // The triangles (three vertex indices for each triangle)
        int indices[] = {0,2,3, 0,1,2,			// front face
                4,6,7, 4,5,6,			// left face
                8,10,11, 8,9,10,		// back face
                12,14,15, 12,13,14,	// right face
                16,18,19, 16,17,18,	// top face
                20,22,23, 20,21,22};	// bottom face

        vertexData.addIndices(indices);

        return vertexData;
    }

    public static final Shape makeHouse(RenderContext renderContext)
    {
        // A house
        float vertices[] = {-4,-4,4, 4,-4,4, 4,4,4, -4,4,4,		// front face
                -4,-4,-4, -4,-4,4, -4,4,4, -4,4,-4, // left face
                4,-4,-4,-4,-4,-4, -4,4,-4, 4,4,-4,  // back face
                4,-4,4, 4,-4,-4, 4,4,-4, 4,4,4,		// right face
                4,4,4, 4,4,-4, -4,4,-4, -4,4,4,		// top face
                -4,-4,4, -4,-4,-4, 4,-4,-4, 4,-4,4, // bottom face

                -20,-4,20, 20,-4,20, 20,-4,-20, -20,-4,-20, // ground floor
                -4,4,4, 4,4,4, 0,8,4,				// the roof
                4,4,4, 4,4,-4, 0,8,-4, 0,8,4,
                -4,4,4, 0,8,4, 0,8,-4, -4,4,-4,
                4,4,-4, -4,4,-4, 0,8,-4};

        float normals[] = {0,0,1,  0,0,1,  0,0,1,  0,0,1,		// front face
                -1,0,0, -1,0,0, -1,0,0, -1,0,0,		// left face
                0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1,		// back face
                1,0,0,  1,0,0,  1,0,0,  1,0,0,		// right face
                0,1,0,  0,1,0,  0,1,0,  0,1,0,		// top face
                0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0,		// bottom face

                0,1,0,  0,1,0,  0,1,0,  0,1,0,		// ground floor
                0,0,1,  0,0,1,  0,0,1,				// front roof
                0.707f,0.707f,0, 0.707f,0.707f,0, 0.707f,0.707f,0, 0.707f,0.707f,0, // right roof
                -0.707f,0.707f,0, -0.707f,0.707f,0, -0.707f,0.707f,0, -0.707f,0.707f,0, // left roof
                0,0,-1, 0,0,-1, 0,0,-1};				// back roof

        float colors[] = {1,0,0, 1,0,0, 1,0,0, 1,0,0,
                0,1,0, 0,1,0, 0,1,0, 0,1,0,
                1,0,0, 1,0,0, 1,0,0, 1,0,0,
                0,1,0, 0,1,0, 0,1,0, 0,1,0,
                0,0,1, 0,0,1, 0,0,1, 0,0,1,
                0,0,1, 0,0,1, 0,0,1, 0,0,1,

                0,0.5f,0, 0,0.5f,0, 0,0.5f,0, 0,0.5f,0,			// ground floor
                0,0,1, 0,0,1, 0,0,1,							// roof
                1,0,0, 1,0,0, 1,0,0, 1,0,0,
                0,1,0, 0,1,0, 0,1,0, 0,1,0,
                0,0,1, 0,0,1, 0,0,1,};

        // Set up the vertex data
        VertexData vertexData = renderContext.makeVertexData(42);

        // Specify the elements of the vertex data:
        // - one element for vertex positions
        vertexData.addElement(vertices, VertexData.Semantic.POSITION, 3);
        // - one element for vertex colors
        vertexData.addElement(colors, VertexData.Semantic.COLOR, 3);
        // - one element for vertex normals
        vertexData.addElement(normals, VertexData.Semantic.NORMAL, 3);

        // The index data that stores the connectivity of the triangles
        int indices[] = {0,2,3, 0,1,2,			// front face
                4,6,7, 4,5,6,			// left face
                8,10,11, 8,9,10,		// back face
                12,14,15, 12,13,14,	// right face
                16,18,19, 16,17,18,	// top face
                20,22,23, 20,21,22,	// bottom face

                24,26,27, 24,25,26,	// ground floor
                28,29,30,				// roof
                31,33,34, 31,32,33,
                35,37,38, 35,36,37,
                39,40,41};

        vertexData.addIndices(indices);

        Shape house = new Shape(vertexData);

        return house;
    }
    public static final Shape makeTorus(int innerResolution, int outerResolution, float innerRadius, float outerRadius, RenderContext renderContext)
    {
        assert innerResolution >= 3;
        assert outerResolution >= 3;

        int numberOfVertices = innerResolution * outerResolution;
        float[] normals = new float[3*numberOfVertices];    //process normals
        float[] uv = new float[2*numberOfVertices];
        // The vertex positions
        float[] v = new float[3*numberOfVertices]; // xyz per vertex
        double angleBetweenTwoInnerSegments = (2*Math.PI)/innerResolution;
        double angleBetweenTwoOuterSegments = (2*Math.PI)/outerResolution;
        double currentInnerAngle = 0;
        double currentOuterAngle = 0;

        for(int i=0; i<innerResolution; i++) // loop over inner segments
        {
            for(int j=0; j<3*outerResolution; j+=3) // loop over outer segments
            {
                float x = (float) ( (innerRadius + outerRadius * Math.cos(currentOuterAngle)) * Math.cos(currentInnerAngle) );
                float y = (float) ( outerRadius * Math.sin(currentOuterAngle) );
                float z = (float) ( (innerRadius + outerRadius * Math.cos(currentOuterAngle)) * Math.sin(currentInnerAngle) );

                v[i*3*outerResolution + j] = x;
                v[i*3*outerResolution + j+1] = y;
                v[i*3*outerResolution + j+2] = z;

                normals[i*3*outerResolution + j] =(float) ( (innerRadius + outerRadius * Math.cos(currentOuterAngle)) * Math.cos(currentInnerAngle) );
                normals[i*3*outerResolution + j+2] = (float) ( (innerRadius + outerRadius * Math.cos(currentOuterAngle)) * Math.sin(currentInnerAngle) );

                currentOuterAngle += angleBetweenTwoOuterSegments; // reach next outer segment
            }
            currentOuterAngle = 0; // start building the circle again
            currentInnerAngle += angleBetweenTwoInnerSegments; // reach next inner segment
        }

        // The vertex colors
        float c[] = new float[3*numberOfVertices]; // rgb per vertex

        for(int i=0; i<innerResolution; i++) // loop over inner segments
        {
            float r,g,b;
            r = (float) Math.random();
            g = (float) Math.random();
            b = (float) Math.random();

            for(int j=0; j<3*outerResolution; j+=3) // loop over outer segments
            {
                c[i*3*outerResolution + j] = r;
                c[i*3*outerResolution + j+1] = g;
                c[i*3*outerResolution + j+2] = b;
            }
        }

        // The triangles
        int indices[] = new int[2*(innerResolution * outerResolution)*3]; // two triangles per face, inner- times outer resolution faces and three vertex indices for each triangle
        for(int i=0; i<innerResolution; i++) // loop over inner resolution
        {
            for(int j=0; j<outerResolution; j++) // loop over outer resolution
            {
                // build one side
                indices[i*2*3*outerResolution + 6*j] = (i*outerResolution +j)%numberOfVertices; // two triangles, each three indices, outer resolution many faces of segment
                indices[i*2*3*outerResolution + 6*j +1] = (i*outerResolution +j+1)%numberOfVertices;
                indices[i*2*3*outerResolution + 6*j +2] = ((i+1)*outerResolution +j)%numberOfVertices;

                indices[i*2*3*outerResolution + 6*j +3] = ((i+1)*outerResolution +j)%numberOfVertices;
                indices[i*2*3*outerResolution + 6*j +4] = (i*outerResolution +j+1)%numberOfVertices;
                indices[i*2*3*outerResolution + 6*j +5] = ((i+1)*outerResolution +j+1)%numberOfVertices;
            }
        }

        currentInnerAngle = 0;
        currentOuterAngle = 0;
        for(int i=0; i<innerResolution; i++) // loop over inner segments
        {
            for(int j=0; j<2*outerResolution; j+=2) // loop over outer segments
            {
                 uv[i*2*outerResolution+j]= (float)currentInnerAngle / (float)(2*Math.PI);
                 uv[i*2*outerResolution+j+1]= (float)currentOuterAngle/ (float)(2*Math.PI);
                currentOuterAngle += angleBetweenTwoOuterSegments; // reach next outer segment
            }

            currentOuterAngle = 0; // start building the circle again
            currentInnerAngle += angleBetweenTwoInnerSegments; // reach next inner segment
            currentInnerAngle %=2*Math.PI;
        }

        VertexData vertexData = renderContext.makeVertexData(numberOfVertices);
        vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
        vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
        vertexData.addElement(normals,VertexData.Semantic.NORMAL,3);
        vertexData.addElement(uv,VertexData.Semantic.TEXCOORD,2);
        vertexData.addIndices(indices);

        Shape torus = new Shape(vertexData);
        return torus;
    }

}