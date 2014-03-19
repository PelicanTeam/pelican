package fr.unistra.pelican.util.neighbourhood;

import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.PointVideo;

public class NeighbourhoodVideo {
	
	/**
	 * Returns the point of the 6-temporalneighbourhood
	 * (-1,0,0),(0,-1,0),(1,0,0),(0,1,0),(0,0,-1),(0,0,1) 
	 * @return 6-temoralneighbourhood
	 */
	public static PointVideo[] get6TemporalNeighboorhood()
	{
		PointVideo[] neighbourhood = {new PointVideo(-1,0,0),new PointVideo(0,-1,0),new PointVideo(1,0,0),new PointVideo(0,1,0),new PointVideo(0,0,1),new PointVideo(0,0,-1)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the 10-temporalneighbourhood
	 * (-1,-1,0),(0,-1,0),(1,-1,0),(-1,0,0),(1,0,0),(-1,1,0),(0,1,0),(1,1,0),(0,0,1),(0,0,-1)
	 * @return 10-temporalneighbourhood
	 */
	public static PointVideo[] get10TemporalNeighboorhood()
	{
		PointVideo[] neighbourhood = {new PointVideo(-1,-1,0),new PointVideo(0,-1,0),new PointVideo(1,-1,0),new PointVideo(-1,0,0),new PointVideo(1,0,0),new PointVideo(-1,1,0),new PointVideo(0,1,0),new PointVideo(1,1,0),new PointVideo(0,0,1),new PointVideo(0,0,-1)};
		return neighbourhood;
	}
	/**
	 * Returns the point of the semi 10-temporalneighbourhood
	 * (1,0,0),(-1,1,0),(0,1,0),(1,1,0),(0,0,1)
	 * @return 10-temporalneighbourhood
	 */
	public static PointVideo[] getSemi10TemporalNeighboorhood()
	{
		PointVideo[] neighbourhood = {new PointVideo(1,0,0),new PointVideo(-1,1,0),new PointVideo(0,1,0),new PointVideo(1,1,0),new PointVideo(0,0,1)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the 26-temporalneighbourhood
	 * (-1,-1,0),(0,-1,0),(1,-1,0),(-1,0,0),(1,0,0),(-1,1,0),(0,1,0),(1,1,0),
	 * (-1,-1,1),(0,-1,1),(1,-1,1),(-1,0,1),(0,0,1),(1,0,1),(-1,1,1),(0,1,1),(1,1,1),
	 * (-1,-1,-1),(0,-1,-1),(1,-1,-1),(-1,0,-1),(0,0,-1),(1,0,-1),(-1,1,-1),(0,1,-1),(1,1,-1)
	 * @return 26-temporalneighbourhood
	 */
	public static PointVideo[] get26TemporalNeighboorhood()
	{
		PointVideo[] neighbourhood = {new PointVideo(-1,-1,0),new PointVideo(0,-1,0),new PointVideo(1,-1,0),new PointVideo(-1,0,0),new PointVideo(1,0,0),new PointVideo(-1,1,0),new PointVideo(0,1,0),new PointVideo(1,1,0),
				new PointVideo(-1,-1,1),new PointVideo(0,-1,1),new PointVideo(1,-1,1),new PointVideo(-1,0,1),new PointVideo(0,0,1),new PointVideo(1,0,1),new PointVideo(-1,1,1),new PointVideo(0,1,1),new PointVideo(1,1,1),
				new PointVideo(-1,-1,-1),new PointVideo(0,-1,-1),new PointVideo(1,-1,-1),new PointVideo(-1,0,-1),new PointVideo(0,0,-1),new PointVideo(1,0,-1),new PointVideo(-1,1,-1),new PointVideo(0,1,-1),new PointVideo(1,1,-1)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the 2-temporal neighbourhood
	 * (0,0,-1),(0,0,1) 
	 * @return 2-temoralneighbourhood
	 */
	public static PointVideo[] get2TemporalNeighboorhood()
	{
		PointVideo[] neighbourhood = {new PointVideo(0,0,1),new PointVideo(0,0,-1)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the semi 2-temporal neighbourhood
	 * (0,0,1) 
	 * @return semi 2-temoralneighbourhood
	 */
	public static PointVideo[] getSemi2TemporalNeighboorhood()
	{
		PointVideo[] neighbourhood = {new PointVideo(0,0,1)};
		return neighbourhood;
	}
	
	public static PointVideo[] getTotalTemporalNeighboorhood(int tdim)
	{
		PointVideo[] neighbourhood = new PointVideo[(tdim-1)*2];
		int neighIndex=0;
		for(int i=1;i<tdim;i++)
		{
			neighbourhood[neighIndex++]=new PointVideo(0,0,i);
			neighbourhood[neighIndex++]=new PointVideo(0,0,-i);
		}
		return neighbourhood;
	}
	
	public static PointVideo[] getSemiTotalTemporalNeighboorhood(int tdim)
	{
		PointVideo[] neighbourhood = new PointVideo[(tdim-1)];
		int neighIndex=0;
		for(int i=1;i<tdim;i++)
		{
			neighbourhood[neighIndex++]=new PointVideo(0,0,i);
		}
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the semi 8-neighbourhood
	 * (1,0,0),(-1,1,0),(0,1,0),(1,1,0)
	 * @return semi 8-neighbourhood
	 */
	public static PointVideo[] getSemi8Neighboorhood()
	{
		PointVideo[] neighbourhood = {new PointVideo(1,0,0),new PointVideo(-1,1,0),new PointVideo(0,1,0),new PointVideo(1,1,0)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the semi 4-neighbourhood
	 * (1,0,0),(0,1,0) 
	 * @return semi 4-neighbourhood
	 */
	public static PointVideo[] getSemi4Neighboorhood()
	{
		PointVideo[] neighbourhood = {new PointVideo(1,0,0),new PointVideo(0,1,0)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the 4-neighbourhood
	 * (-1,0,0),(0,-1,0),(1,0,0),(0,1,0) 
	 * @return 4-neighbourhood
	 */
	public static PointVideo[] get4Neighboorhood()
	{
		PointVideo[] neighbourhood = {new PointVideo(-1,0,0),new PointVideo(0,-1,0),new PointVideo(1,0,0),new PointVideo(0,1,0)};
		return neighbourhood;
	}
	
	/**
	 * Returns the point of the 8-neighbourhood
	 * (-1,-1,0),(0,-1,0),(1,-1,0),(-1,0,0),(1,0,0),(-1,1,0),(0,1,0),(1,1,0)
	 * @return 8-neighbourhood
	 */
	public static PointVideo[] get8Neighboorhood()
	{
		PointVideo[] neighbourhood = {new PointVideo(-1,-1,0),new PointVideo(0,-1,0),new PointVideo(1,-1,0),new PointVideo(-1,0,0),new PointVideo(1,0,0),new PointVideo(-1,1,0),new PointVideo(0,1,0),new PointVideo(1,1,0)};
		return neighbourhood;
	}

}
