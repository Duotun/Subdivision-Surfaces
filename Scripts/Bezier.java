package Mesh;

import javax.vecmath.Vector3f;
import jrtr.RenderContext;
import jrtr.Shape;
import jrtr.VertexData;
import sun.nio.cs.ext.MacThai;

import java.awt.*;

public class Bezier {
  //return tangent vectors, cubic bezier (four control points)
  public static Vector3f [][] BezierCurves(Vector3f[] ControlPoints, int N) // N is equal to number of points
  {
      Vector3f[][] res = new Vector3f[2][];
      Vector3f[] points = new Vector3f[N];
      Vector3f[] tangentvectors = new Vector3f[N];

      //coefficients
      float ax = -1f*ControlPoints[0].x+3f*ControlPoints[1].x-3f*ControlPoints[2].x+ControlPoints[3].x;
      float ay = -1f*ControlPoints[0].y+3f*ControlPoints[1].y-3f*ControlPoints[2].y+ControlPoints[3].y;
      float az = -1f*ControlPoints[0].z+3f*ControlPoints[1].z-3f*ControlPoints[2].z+ControlPoints[3].z;

      float bx = 3f*ControlPoints[0].x-6f*ControlPoints[1].x+3f*ControlPoints[2].x;
      float by = 3f*ControlPoints[0].y-6f*ControlPoints[1].y+3f*ControlPoints[2].y;
      float bz = 3f*ControlPoints[0].z-6f*ControlPoints[1].z+3f*ControlPoints[2].z;

      float cx = -3f*ControlPoints[0].x+3f*ControlPoints[1].x;
      float cy = -3f*ControlPoints[0].y+3f*ControlPoints[1].y;
      float cz = -3f*ControlPoints[0].z+3f*ControlPoints[1].z;

      float dx = 1f*ControlPoints[0].x;
      float dy = 1f*ControlPoints[0].y;
      float dz = 1f*ControlPoints[0].z;
      for(int i=0;i<N;i++)
      {

          float t_i = (float)i /(float)(N-1);
          float x_ix= ax* (float)Math.pow(t_i,3)+bx* (float)Math.pow(t_i,2)+cx*t_i+dx;
          float x_iy= ay* (float)Math.pow(t_i,3)+by* (float)Math.pow(t_i,2)+cy*t_i+dy;
          float x_iz= az* (float)Math.pow(t_i,3)+bz* (float)Math.pow(t_i,2)+cz*t_i+dz;

          points[i] =new Vector3f(x_ix,x_iy,x_iz);


          x_ix = 3*ax*(float)Math.pow(t_i,2)+bx*t_i+cx;
          x_iy = 3*ay*(float)Math.pow(t_i,2)+by*t_i+cy;
          x_iz = 3*az*(float)Math.pow(t_i,2)+bz*t_i+cz;

          tangentvectors[i]=new Vector3f(x_ix,x_iy,x_iz);
      }
      //uniform sampling here
      res[0] = points;
      res[1] = tangentvectors;
      return res;
  }

  //Here N_c segments, the length of controlPoints should be 3N_c+1, each segment has n points
  public static Vector3f [][] PieceWiseBezierCurves(Vector3f[] ControlPoints, int N_c, int N)
  {
      Vector3f[][] res = new Vector3f[2][];
      Vector3f[] points = new Vector3f[N];
      Vector3f[] tangentvectors = new Vector3f[N];

      int cnt=N/N_c;
      int num_n=0;
      for(int i=0;i<3*N_c;i+=3)
      {

          float ax = -1f*ControlPoints[i].x+3f*ControlPoints[i+1].x-3f*ControlPoints[i+2].x+ControlPoints[i+3].x;
          float ay = -1f*ControlPoints[i].y+3f*ControlPoints[i+1].y-3f*ControlPoints[i+2].y+ControlPoints[i+3].y;
          float az = -1f*ControlPoints[i].z+3f*ControlPoints[i+1].z-3f*ControlPoints[i+2].z+ControlPoints[i+3].z;

          float bx = 3f*ControlPoints[i].x-6f*ControlPoints[i+1].x+3f*ControlPoints[i+2].x;
          float by = 3f*ControlPoints[i].y-6f*ControlPoints[i+1].y+3f*ControlPoints[i+2].y;
          float bz = 3f*ControlPoints[i].z-6f*ControlPoints[i+1].z+3f*ControlPoints[i+2].z;

          float cx = -3f*ControlPoints[i].x+3f*ControlPoints[i+1].x;
          float cy = -3f*ControlPoints[i].y+3f*ControlPoints[i+1].y;
          float cz = -3f*ControlPoints[i].z+3f*ControlPoints[i+1].z;

          float dx = 1f*ControlPoints[i].x;
          float dy = 1f*ControlPoints[i].y;
          float dz = 1f*ControlPoints[i].z;
          if(i==3*N_c-3&&(N_c%N)!=0)
          {
              cnt+=N%N_c;   //all the left points assigned to the Last segment
          }
         for(int j =0;j<cnt;j++)
         {
             float t_i= (float)j/(float)(cnt-1);
             float x_ix= ax* (float)Math.pow(t_i,3)+bx* (float)Math.pow(t_i,2)+cx*t_i+dx;
             float x_iy= ay* (float)Math.pow(t_i,3)+by* (float)Math.pow(t_i,2)+cy*t_i+dy;
             float x_iz= az* (float)Math.pow(t_i,3)+bz* (float)Math.pow(t_i,2)+cz*t_i+dz;

             points[num_n] =new Vector3f(x_ix,x_iy,x_iz);


             x_ix = 3*ax*(float)Math.pow(t_i,2)+bx*t_i+cx;
             x_iy = 3*ay*(float)Math.pow(t_i,2)+by*t_i+cy;
             x_iz = 3*az*(float)Math.pow(t_i,2)+bz*t_i+cz;

             tangentvectors[num_n++]=new Vector3f(x_ix,x_iy,x_iz);
         }

      }
      //uniform sampling here
      res[0] = points;
      res[1] = tangentvectors;
      return res;
  }

  //x, y coordinates
   // k -> rotation steps can be considered as rotationresolution
  public static Shape CreateRotationalSurface(int K, Vector3f []Points, Vector3f[] tangentvectors, RenderContext renderContext)
  {
      int curveresolution = Points.length;
      int rotationresolution = K;
      int numberOfVertices = curveresolution *rotationresolution;
      float v[] = new float[3*numberOfVertices];
      float uv[]= new float[2*numberOfVertices];
      //normals
      //first nomalized the tangent vectors
      float []normals = new float[3*numberOfVertices];
      float tmpvalue;
      for(int i=0;i<curveresolution;i++)   //first line
      {
          v[3*i]= Points[i].x;
          v[3*i+1]= Points[i].y;
          v[3*i+2] =0;

          //process tangentvectors
          tmpvalue = tangentvectors[i].x;
          tangentvectors[i].x = -tangentvectors[i].y;
          tangentvectors[i].y =tmpvalue;
          tangentvectors[i].z =0;
          tangentvectors[i].normalize();

          normals[3*i] = -tangentvectors[i].x;
          normals[3*i+1] = -tangentvectors[i].y;
          normals[3*i+2] =0;

          uv[2*i]=0;
          uv[2*i+1] = (float)i /(float)curveresolution;
      }

      double angle = 2* Math.PI/rotationresolution;
      double stepangle = angle;
      for(int i=1;i<rotationresolution;i++)   //expand
      {
          for(int j=0;j<curveresolution;j++)
          {
              v[3*(i*curveresolution+j)]=(float)(v[3*j]*Math.cos(angle)- v[3*j+2] * Math.sin(angle));
              v[3*(i*curveresolution+j)+1]=v[3*j+1];
              v[3*(i*curveresolution+j)+2]=(float)(v[3*j]*Math.sin(angle)+ v[3*j+2] * Math.cos(angle));

              normals[3*(i*curveresolution+j)]=(float)(normals[3*j]*Math.cos(angle)- normals[3*j+2] * Math.sin(angle));
              normals[3*(i*curveresolution+j)+1]=normals[3*j+1];
              normals[3*(i*curveresolution+j)+2]=(float)(normals[3*j]*Math.sin(angle)+ normals[3*j+2] * Math.cos(angle));

              uv[2*(i*curveresolution+j)] = (float)i /(float)rotationresolution;
              uv[2*(i*curveresolution+j)+1]=  (float)j/(float)curveresolution;
          }
          angle+=stepangle;
      }
      //The triangles (three vertex indices for each triangle), numbers are decided by the number of segments
      int [] indices=new int[2*numberOfVertices*3];
      for(int i=0;i<rotationresolution;i++)
      {
          for(int j=0;j<curveresolution;j++)
          {
              indices[6*(i*curveresolution+j)] = (i*curveresolution+j)%numberOfVertices;
              indices[6*(i*curveresolution+j)+1] = (i*curveresolution+j+1)%numberOfVertices;
              indices[6*(i*curveresolution+j)+2] = ((i+1)*curveresolution+j)%numberOfVertices;

              indices[6*(i*curveresolution+j)+3] = (i*curveresolution+j+1)%numberOfVertices;
              indices[6*(i*curveresolution+j)+4] = ((i+1)*curveresolution+j+1)%numberOfVertices;
              indices[6*(i*curveresolution+j)+5] = ((i+1)*curveresolution+j)%numberOfVertices;

          }
      }

      float []c = new float[3*numberOfVertices];
      for(int i=0; i<rotationresolution; i++)  //render color
      {
          float r,g,b;
          r = (float) Math.random();
          g = (float) Math.random();
          b = (float) Math.random();

          r = 0.6f;  //for apples
          for(int j=0; j<3*curveresolution; j+=3)
          {
              c[i*3*curveresolution + j] = r;
              c[i*3*curveresolution + j+1] = 0;
              c[i*3*curveresolution + j+2] = 0;
          }
      }



      VertexData vertexData = renderContext.makeVertexData(numberOfVertices);
      vertexData.addElement(c,VertexData.Semantic.COLOR,3);
      vertexData.addElement(v,VertexData.Semantic.POSITION,3);
      vertexData.addElement(normals,VertexData.Semantic.NORMAL,3);
      vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);
      vertexData.addIndices(indices);
      Shape surfaceshape = new Shape(vertexData);
      return surfaceshape;
  }


}
