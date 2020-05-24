/*
 * Copyright (c) 2010 ThingMagic, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
using System;
using System.Collections.Generic;

namespace ThingMagic
{
    /// <summary>
    /// Iso tag-specific constructs
    /// </summary>
    public static class Ipx64
    {
        #region Nested Enums

        #endregion

        #region Nested Classes

        #region TagData

        /// <summary>
        /// iPx specific version of TagData.
        /// </summary>
        public class TagData : ThingMagic.TagData
        {
            #region Properties

            /// <summary>
            /// Tag's RFID protocol
            /// </summary>
            public override TagProtocol Protocol
            {
                get
                {
                    return TagProtocol.IPX64;
                }
            }

            #endregion

            #region Construction

            /// <summary>
            /// Create TagData with blank CRC
            /// </summary>
            /// <param name="epcBytes">EPC value</param>
            public TagData(ICollection<byte> epcBytes) : base(epcBytes) { }

            /// <summary>
            /// Create TagData
            /// </summary>
            /// <param name="epcBytes">EPC value</param>
            /// <param name="crcBytes">CRC value</param>
            public TagData(ICollection<byte> epcBytes, ICollection<byte> crcBytes) : base(epcBytes, crcBytes) { } 

            #endregion
        }

        #endregion


        #endregion

    }

    /// <summary>
    /// ISO tag-specific constructs for Ipx256
    /// </summary>
    public static class Ipx256
    {
        /// <summary>
        /// The data structure holding the data read from tag
        /// </summary>
        public class TagData : ThingMagic.TagData
	{
            #region Properties

            /// <summary>
            /// Tag's RFID protocol
            /// </summary>
            public override TagProtocol Protocol
            {
                get
                {
                    return TagProtocol.IPX256;
                }
            }

            #endregion

            #region Construction

            /// <summary>
            /// Create TagData with blank CRC
            /// </summary>
            /// <param name="epcBytes">EPC value</param>
            public TagData(ICollection<byte> epcBytes) : base(epcBytes) { }

            /// <summary>
            /// Create TagData
            /// </summary>
            /// <param name="epcBytes">EPC value</param>
            /// <param name="crcBytes">CRC value</param>
            public TagData(ICollection<byte> epcBytes, ICollection<byte> crcBytes) : base(epcBytes, crcBytes) { } 

            #endregion
	}
    } 

}
