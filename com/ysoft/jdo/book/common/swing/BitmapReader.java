/*
Copyright (c) 2002 Yankee Software.

This file is part of the JDO Learning Tools

The JDO Learning Tools is free software; you can use it, redistribute it,
and/or modify it under the terms of the GNU General Public License as
published by the Free Software Foundation; either version 2 of the
License, or (at your option) any later version.

The JDO Learning Tools software is distributed in the hope that it
will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
the GNU General Public License for more details.

A copy of the GPL Version 2 is contained in LICENSE.TXT in this source
distribution.  If you cannot find LICENSE.TXT, write to the Free
Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA or visit www.fsf.org on the web.

Copyright law and the license agreement do not apply to your
understanding of the the concepts, principles, and practices embedded
in this code.  The purpose of the JDO Learning Tools to to help
advance the use and understanding of Java Data Objects, the standard
for transparent persistence for Java objects from the Java Community
Process.

Change History:

Please insert a brief record of any changes made.

Author            Date        Purpose
-----------------+----------+-----------------------------------
David Ezzio       09/01/02   Created
*/
package com.ysoft.jdo.book.common.swing;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;


/**
 * This class reads a bitmap file in one fell swoop.  The resulting image is then available
 * for use in the ordinary way.  The image is read during object construction.  Therefore,
 * successful construction of a BitmapReader means that the image is available.
 */
public class BitmapReader
   {
   private static boolean DEBUG = false; // determines whether info strings are created

   //static private boolean DEBUG = true; // determines whether info strings are created
   private static int BITMAP_FILEHEADER_LENGTH  = 14;
   private static int BITMAP_INFOHEADER_LENGTH  = 40;
   private static int EIGHT_BITS_PER_PIXEL      = 8;
   private static int TWENTYFOUR_BITS_PER_PIXEL = 24;
   private Vector     infoStrings;
   private Image      image;
   private Dimension  imageSize;
   private int        bits_per_pixel;

   public BitmapReader(String filename)
         throws IOException, FileFormatException
      {
      this(null, filename);
      }

   public BitmapReader(String dirname, String filename)
         throws IOException, FileFormatException
      {
      this(new File(dirname, filename));
      addInfoString("File found in directory: " + dirname +
         "; and file name was: " + filename);

      /*
      if (DEBUG)
         infoStrings = new Vector();

      addInfoString("Directory: " + dirname + "; file name: " + filename);
      BufferedInputStream stream = new BufferedInputStream(new FileInputStream(
            new File(dirname, filename)));

      read(stream);
      */
      }

   public BitmapReader(File file)
         throws IOException, FileFormatException
      {
      this(new BufferedInputStream(new FileInputStream(file)));
      addInfoString("Buffered input stream read file: " +
         file.getAbsolutePath());

      /*
      if (DEBUG)
         infoStrings = new Vector();

      addInfoString("Filename: " + file.getAbsolutePath());
      BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));

      read(stream);
      */
      }

   public BitmapReader(BufferedInputStream stream)
         throws IOException, FileFormatException
      {
      if (DEBUG)
         infoStrings = new Vector();

      addInfoString("Reading from supplied buffered input stream");
      read(stream);
      }

   public BitmapReader()
         throws IOException, FileFormatException
      {
      if (DEBUG)
         infoStrings = new Vector();

      addInfoString("Shading Black to Red and Black to Blue internal image");

      int   w     = 400;
      int   h     = 400;
      int[] pix   = new int[w * h];
      int   index = 0;

      for (int y = 0; y < h; y++)
         {
         int red = (y * 255) / (h - 1);

         for (int x = 0; x < w; x++)
            {
            int blue = (x * 255) / (w - 1);

            //pix[index++] = (255 << 24) | (red << 16) | blue;
            pix[index++] = 0xff000000 | (red << 16) | blue;
            }
         }

      image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w,
               h, pix, 0, w));
      }

   public String[] getInfoStrings()
      {
      if (infoStrings != null)
         {
         String[] retv = new String[infoStrings.size()];
         infoStrings.copyInto(retv);
         return retv;
         }

      return null;
      }

   public Dimension getSize()
      {
      return imageSize;
      }

   public int getColorDepth()
      {
      return bits_per_pixel;
      }

   public Image getImage()
      {
      return image;
      }

   private void read(BufferedInputStream stream)
         throws IOException, FileFormatException
      {
      try
         {
         // read the file header
         byte[] bitmap_file_header = new byte[BITMAP_FILEHEADER_LENGTH];

         if (stream.read(bitmap_file_header) != BITMAP_FILEHEADER_LENGTH)
            throw new FileFormatException("Missing file header");

         char first_char  = (char) bitmap_file_header[0];
         char second_char = (char) bitmap_file_header[1];
         addInfoString("File type is: " + first_char + second_char);

         if ((first_char != 'B') || (second_char != 'M'))
            throw new FileFormatException("Not a bitmap file", infoStrings);

         int nsize = (((int) bitmap_file_header[5] & 0xff) << 24) |
                  (((int) bitmap_file_header[4] & 0xff) << 16) |
                  (((int) bitmap_file_header[3] & 0xff) << 8) |
                  ((int) bitmap_file_header[2] & 0xff);
         addInfoString("Size of file is: " + nsize);

         if (nsize <= 0)
            {
            nsize = stream.available() + 14;
            addInfoString("Size of file adjusted to: " + nsize);
            }

         int offset = (((int) bitmap_file_header[13]) << 24) |
                  (((int) bitmap_file_header[12]) << 16) |
                  (((int) bitmap_file_header[11]) << 8) |
                  ((int) bitmap_file_header[10]);
         addInfoString("offset to pixel data is: " + offset);

         if (offset < (BITMAP_FILEHEADER_LENGTH + BITMAP_INFOHEADER_LENGTH))
            {
            offset = BITMAP_FILEHEADER_LENGTH + BITMAP_INFOHEADER_LENGTH;
            addInfoString("offset adjusted to 54");
            }

         // read the bitmap header
         byte[] bitmap_info_header = new byte[BITMAP_INFOHEADER_LENGTH];

         if (stream.read(bitmap_info_header) != BITMAP_INFOHEADER_LENGTH)
            throw new FileFormatException("Missing bitmap header", infoStrings);

         int nbisize = (((int) bitmap_info_header[3] & 0xff) << 24) |
                  (((int) bitmap_info_header[2] & 0xff) << 16) |
                  (((int) bitmap_info_header[1] & 0xff) << 8) |
                  ((int) bitmap_info_header[0] & 0xff);
         addInfoString("Size of bitmapinfoheader is: " + nbisize);

         int nwidth = (((int) bitmap_info_header[7] & 0xff) << 24) |
                  (((int) bitmap_info_header[6] & 0xff) << 16) |
                  (((int) bitmap_info_header[5] & 0xff) << 8) |
                  ((int) bitmap_info_header[4] & 0xff);
         addInfoString("Width is: " + nwidth);

         int nheight = (((int) bitmap_info_header[11] & 0xff) << 24) |
                  (((int) bitmap_info_header[10] & 0xff) << 16) |
                  (((int) bitmap_info_header[9] & 0xff) << 8) |
                  ((int) bitmap_info_header[8] & 0xff);
         addInfoString("Height is: " + nheight);

         imageSize = new Dimension(nwidth, nheight);

         int nplanes = (((int) bitmap_info_header[13] & 0xff) << 8) |
                  ((int) bitmap_info_header[12] & 0xff);
         addInfoString("Planes is: " + nplanes);

         int bits_per_pixel = (((int) bitmap_info_header[15] & 0xff) << 8) |
                  ((int) bitmap_info_header[14] & 0xff);
         addInfoString("Bits per pixel is: " + bits_per_pixel);

         // Look for non-zero values to indicate compression
         int ncompression = (((int) bitmap_info_header[19]) << 24) |
                  (((int) bitmap_info_header[18]) << 16) |
                  (((int) bitmap_info_header[17]) << 8) |
                  (int) bitmap_info_header[16];
         addInfoString("Compression is: " + ncompression);

         int nsizeimage = (((int) bitmap_info_header[23] & 0xff) << 24) |
                  (((int) bitmap_info_header[22] & 0xff) << 16) |
                  (((int) bitmap_info_header[21] & 0xff) << 8) |
                  ((int) bitmap_info_header[20] & 0xff);
         addInfoString("SizeImage is: " + nsizeimage);

         int nxpm = (((int) bitmap_info_header[27] & 0xff) << 24) |
                  (((int) bitmap_info_header[26] & 0xff) << 16) |
                  (((int) bitmap_info_header[25] & 0xff) << 8) |
                  ((int) bitmap_info_header[24] & 0xff);
         addInfoString("X-Pixels per meter is: " + nxpm);

         int nypm = (((int) bitmap_info_header[31] & 0xff) << 24) |
                  (((int) bitmap_info_header[30] & 0xff) << 16) |
                  (((int) bitmap_info_header[29] & 0xff) << 8) |
                  ((int) bitmap_info_header[28] & 0xff);
         addInfoString("Y-Pixels per meter is: " + nypm);

         int nclrused = (((int) bitmap_info_header[35] & 0xff) << 24) |
                  (((int) bitmap_info_header[34] & 0xff) << 16) |
                  (((int) bitmap_info_header[33] & 0xff) << 8) |
                  ((int) bitmap_info_header[32] & 0xff);
         addInfoString("Colors used are: " + nclrused);

         int nclrimp = (((int) bitmap_info_header[39] & 0xff) << 24) |
                  (((int) bitmap_info_header[38] & 0xff) << 16) |
                  (((int) bitmap_info_header[37] & 0xff) << 8) |
                  ((int) bitmap_info_header[36] & 0xff);
         addInfoString("Colors important are: " + nclrimp);

         if (bits_per_pixel == TWENTYFOUR_BITS_PER_PIXEL)
            {
            // No Palette data for 24-bit format but pixel data
            // per scan line is padded to 32 bit boundaries.
            int npad = 0;

            if (nsizeimage <= 0)
               {
               nsizeimage = nsize - offset;
               addInfoString("Adjusted SizeImage to: " + nsizeimage);
               }

            npad = (nsizeimage / nheight) - (nwidth * 3);
            addInfoString("calculated npad is: " + npad);

            if ((npad < 0) || (npad > 3))
               throw new FileFormatException("illegal calculated pad value: " +
                  npad, infoStrings);

            int[] ndata = new int[nheight * nwidth];

            //int bytes_to_read = (nwidth + npad) * 3 * nheight; bug: npad is already a 3 multiple
            int bytes_to_read = ((nwidth * 3) + npad) * nheight;
            addInfoString("calculated bytes to read: " + bytes_to_read);

            byte[] brgb         = new byte[bytes_to_read];
            int    read_to_data = offset - BITMAP_FILEHEADER_LENGTH -
               BITMAP_INFOHEADER_LENGTH;

            if (read_to_data > 0)
               {
               addInfoString("Skipping " + read_to_data +
                  " bytes to reach pixel data");

               if (stream.skip(read_to_data) != read_to_data)
                  throw new FileFormatException("missing data", infoStrings);
               }

            int bytes_read = stream.read(brgb);
            addInfoString("actually number of pixel bytes read: " + bytes_read);

            if (bytes_read != bytes_to_read)
               throw new FileFormatException("Expected pixel info not found",
                  infoStrings);

            int nindex = 0;

            for (int j = 0; j < nheight; j++)
               {
               for (int i = 0; i < nwidth; i++)
                  {
                  ndata[(nwidth * (nheight - j - 1)) + i] = (int) 0xff000000 |
                           (((int) brgb[nindex + 2] & 0xff) << 16) |
                           (((int) brgb[nindex + 1] & 0xff) << 8) |
                           ((int) brgb[nindex] & 0xff);

                  // addInfoString("Encoded Color at ("
                  //+i+","+j+")is:"+nrgb+" (R,G,B)= ("
                  //+((int)(brgb[2]) & 0xff)+","
                  //+((int)brgb[1]&0xff)+","
                  //+((int)brgb[0]&0xff)+")");
                  nindex += 3;
                  }

               nindex += npad;
               }

            image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(
                     nwidth, nheight, ndata, 0, nwidth));
            }
         else if (bits_per_pixel == EIGHT_BITS_PER_PIXEL)
            {
            // Have to determine the number of colors, the clrsused
            // parameter is dominant if it is greater than zero. If
            // zero, calculate colors based on bitsperpixel.
            int nNumColors = 0;

            if (nclrused > 0)
               {
               nNumColors = nclrused;
               }
            else
               {
               //nNumColors = (1&0xff)<<EIGHT_BITS_PER_PIXEL;
               nNumColors = 256;
               }

            addInfoString("The number of Colors is: " + nNumColors);

            // Some bitmaps do not have the sizeimage field calculated
            // Ferret out these cases and fix 'em.
            if (nsizeimage == 0)
               {
               nsizeimage = ((((nwidth * EIGHT_BITS_PER_PIXEL) + 31) & ~31) >> 3);
               nsizeimage *= nheight;
               addInfoString("nsizeimage (backup) is: " + nsizeimage);
               }

            // Read the palatte colors.
            int[]  npalette = new int[nNumColors];
            byte[] bpalette = new byte[nNumColors * 4];

            if (stream.read(bpalette) != (nNumColors * 4))
               throw new FileFormatException("Missing palette");

            int nindex8 = 0;

            for (int n = 0; n < nNumColors; n++)
               {
               npalette[n] = ((255 & 0xff) << 24) |
                        (((int) bpalette[nindex8 + 2] & 0xff) << 16) |
                        (((int) bpalette[nindex8 + 1] & 0xff) << 8) |
                        ((int) bpalette[nindex8] & 0xff);

               // addInfoString ("Palette Color " + n
               //+" is: " + npalette[n]+" (res,R,G,B)= ("
               //+((int)(bpalette[nindex8+3]) & 0xff)+","
               //+((int)(bpalette[nindex8+2]) & 0xff)+","
               //+((int)bpalette[nindex8+1]&0xff)+","
               //+((int)bpalette[nindex8]&0xff)+")");
               nindex8 += 4;
               }

            // Read the image data (actually indices into the palette)
            // Scan lines are still padded out to even 4-byte
            // boundaries.
            int npad8 = (nsizeimage / nheight) - nwidth;
            addInfoString("nPad is: " + npad8);

            int[] ndata8 = new int[nwidth * nheight];

            int   bytes_to_read = (nwidth + npad8) * nheight;
            addInfoString("calculated bytes to read: " + bytes_to_read);

            byte[] bdata      = new byte[bytes_to_read];
            int    bytes_read = stream.read(bdata);
            addInfoString("actual number of pixel bytes read: " + bytes_read);

            if (bytes_read != bytes_to_read)
               throw new FileFormatException("Expected pixel info missing",
                  infoStrings);

            nindex8 = 0;

            for (int j8 = 0; j8 < nheight; j8++)
               {
               for (int i8 = 0; i8 < nwidth; i8++)
                  {
                  ndata8[(nwidth * (nheight - j8 - 1)) + i8] = npalette[((int) bdata[nindex8] &
                           0xff)];
                  nindex8++;
                  }

               nindex8 += npad8;
               }

            image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(
                     nwidth, nheight, ndata8, 0, nwidth));
            }
         else
            {
            throw new FileFormatException("Not a 24-bit or 8-bit Windows Bitmap",
               infoStrings);
            }

         stream.close();
         }
      catch (RuntimeException e)
         {
         throw new FileFormatException(
            "Caught runtime exception reading bmp file: " + e.getMessage(),
            infoStrings);
         }
      }

   private void addInfoString(String str)
      {
      if (DEBUG)
         infoStrings.add(str);
      }

   public static class FileFormatException extends IOException
      {
      private Vector infoStrings;

      public FileFormatException()
         {
         this(null, null);
         }

      public FileFormatException(String msg)
         {
         this(msg, null);
         }

      public FileFormatException(String msg, Vector infoStrings)
         {
         super(msg);
         this.infoStrings = infoStrings;
         }

      public String[] getInfoStrings()
         {
         if (infoStrings != null)
            {
            String[] retv = new String[infoStrings.size()];
            infoStrings.copyInto(retv);
            return retv;
            }

         return null;
         }
      }
   }
